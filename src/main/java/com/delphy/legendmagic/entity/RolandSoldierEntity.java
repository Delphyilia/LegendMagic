package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.entity.ai.MagicCaster;
import com.delphy.legendmagic.magic.roland.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * ローランド魔導兵（Roland Soldier）
 * スポーン時に兵種クラス（火炎・氷結・雷撃・水流・重力・呪縛）がランダム決定され、
 * 個体ごとに異なるローランド帝国魔法を駆使する精鋭部隊。
 */
public class RolandSoldierEntity extends Monster implements MagicCaster {

    public enum SoldierType {
        LIGHTNING("雷撃兵", new Izuchi(), 35.0D),
        FIRE("火炎兵", new Kurenai(), 28.0D),
        ICE("氷結兵", new Fuen(), 26.0D),
        WATER("水流兵", new Misumi(), 24.0D),
        GRAVITY("重力兵", new Chigashira(), 20.0D),
        BIND("呪縛兵", new Bakushu(), 25.0D);

        private final String title;
        private final Spell spell;
        private final double maxRange;

        SoldierType(String title, Spell spell, double maxRange) {
            this.title = title;
            this.spell = spell;
            this.maxRange = maxRange;
        }

        public String getTitle() { return title; }
        public Spell getSpell() { return spell; }
        public double getMaxRange() { return maxRange; }

        public static SoldierType fromOrdinal(int ordinal) {
            SoldierType[] values = values();
            if (ordinal < 0 || ordinal >= values.length) return LIGHTNING;
            return values[ordinal];
        }
    }

    private static final EntityDataAccessor<Integer> DATA_SOLDIER_TYPE =
            SynchedEntityData.defineId(RolandSoldierEntity.class, EntityDataSerializers.INT);

    public RolandSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        if (!level.isClientSide) {
            // スポーン時にランダムな兵種を割り当て
            SoldierType[] types = SoldierType.values();
            int randIdx = this.random.nextInt(types.length);
            this.setSoldierType(types[randIdx]);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SOLDIER_TYPE, 0);
    }

    public SoldierType getSoldierType() {
        return SoldierType.fromOrdinal(this.entityData.get(DATA_SOLDIER_TYPE));
    }

    public void setSoldierType(SoldierType type) {
        this.entityData.set(DATA_SOLDIER_TYPE, type.ordinal());
        this.setCustomName(Component.literal("ローランド" + type.getTitle()));
        this.setCustomNameVisible(false);
    }

    @Override
    public Component getName() {
        return Component.literal("ローランド魔導兵 (" + getSoldierType().getTitle() + ")");
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                       MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                       @Nullable CompoundTag dataTag) {
        SoldierType[] types = SoldierType.values();
        this.setSoldierType(types[this.random.nextInt(types.length)]);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SoldierType", this.entityData.get(DATA_SOLDIER_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SoldierType")) {
            this.setSoldierType(SoldierType.fromOrdinal(tag.getInt("SoldierType")));
        }
    }

    @Override
    protected void registerGoals() {
        // 優先度1: 兵種ごとの魔法攻撃AI
        this.goalSelector.addGoal(1, new RolandDynamicMagicGoal(this));

        // 優先度2: 近接攻撃（魔法CT中または接近時）
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));

        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D);
    }

    /**
     * 魔法の発動実行
     */
    @Override
    public void performMagicAttack(Spell spell) {
        if (!this.level().isClientSide) {
            spell.execute(this);
            // 複写眼の視界コピー判定用イベント
            MinecraftForge.EVENT_BUS.post(new MagicCastEvent(this, spell));
        }
    }

    /**
     * 兵種に応じた魔法を詠唱・攻撃する動的AI Goal
     */
    static class RolandDynamicMagicGoal extends Goal {
        private final RolandSoldierEntity soldier;
        private int attackTimer = -1;

        public RolandDynamicMagicGoal(RolandSoldierEntity soldier) {
            this.soldier = soldier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.soldier.getTarget();
            if (target == null || !target.isAlive()) return false;
            double maxRange = this.soldier.getSoldierType().getMaxRange();
            return this.soldier.distanceToSqr(target) <= (maxRange * maxRange);
        }

        @Override
        public void start() {
            this.attackTimer = 0;
            Spell spell = this.soldier.getSoldierType().getSpell();
            spell.announceChant(this.soldier);
        }

        @Override
        public void tick() {
            LivingEntity target = this.soldier.getTarget();
            if (target == null) return;

            this.soldier.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.attackTimer++;

            // 詠唱中に腕を振る演出
            if (this.attackTimer % 10 == 0) {
                this.soldier.swing(this.soldier.getUsedItemHand());
            }

            Spell spell = this.soldier.getSoldierType().getSpell();
            int castTime = spell.getCastTime();

            if (this.attackTimer >= castTime) {
                this.soldier.performMagicAttack(spell);
                this.attackTimer = -45; // 魔法後の隙（約2.2秒クールダウン）
            }
        }

        @Override
        public void stop() {
            this.attackTimer = -1;
        }
    }
}