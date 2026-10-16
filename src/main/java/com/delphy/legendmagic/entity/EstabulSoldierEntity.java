package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.entity.ai.MagicCaster;
import com.delphy.legendmagic.magic.estabul.LightSpiritBeast;
import com.delphy.legendmagic.magic.estabul.SkyDancingBeast;
import com.delphy.legendmagic.magic.estabul.SpiritBeast;
import com.delphy.legendmagic.magic.estabul.VortexOfSpirits;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.EnumSet;
import java.util.List;

/**
 * エスタブール魔導兵（Estabul Soldier）
 * 精霊や魔獣と契約を交わしたエスタブール王国の魔導士。
 * 「精獣宿し（自己バフ）」「宙を覆う精霊の力（青い渦による拘束・吸引）」「天空を踊る光の魔獣（爆撃）」を駆使して戦う。
 * プレイヤーは複写眼で視認することにより、エスタブール体系の高等魔法を解析・習得できる。
 */
public class EstabulSoldierEntity extends Monster implements MagicCaster {

    private static final List<Spell> ATTACK_SPELLS = List.of(
            new VortexOfSpirits(),
            new SkyDancingBeast(),
            new LightSpiritBeast()
    );
    private static final Spell BUFF_SPELL = new SpiritBeast();

    public EstabulSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EstabulMagicGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    public void performMagicAttack(Spell spell) {
        if (!this.level().isClientSide) {
            spell.execute(this);
            MinecraftForge.EVENT_BUS.post(new MagicCastEvent(this, spell));
        }
    }

    /**
     * エスタブール魔導兵専用の複合魔法AI
     */
    private static class EstabulMagicGoal extends Goal {
        private final EstabulSoldierEntity soldier;
        private int castTimer = 0;
        private int buffCooldown = 0;
        private Spell activeSpell = null;

        public EstabulMagicGoal(EstabulSoldierEntity soldier) {
            this.soldier = soldier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.soldier.getTarget();
            return target != null && target.isAlive() && this.soldier.distanceToSqr(target) <= 625.0D; // 25m以内
        }

        @Override
        public void start() {
            this.castTimer = 0;
            // 自己バフが切れていれば優先発動
            if (this.buffCooldown <= 0) {
                this.activeSpell = BUFF_SPELL;
                this.buffCooldown = 600; // 30秒に1回
            } else {
                this.activeSpell = ATTACK_SPELLS.get(this.soldier.random.nextInt(ATTACK_SPELLS.size()));
            }
            this.activeSpell.announceChant(this.soldier);
        }

        @Override
        public void tick() {
            LivingEntity target = this.soldier.getTarget();
            if (target != null) {
                this.soldier.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            this.castTimer++;
            if (this.castTimer % 10 == 0) {
                this.soldier.swing(this.soldier.getUsedItemHand());
            }

            if (this.activeSpell != null && this.castTimer >= this.activeSpell.getCastTime()) {
                this.soldier.performMagicAttack(this.activeSpell);
                this.castTimer = -50; // 2.5秒インターバル
                this.activeSpell = null;
            }

            if (this.buffCooldown > 0) {
                this.buffCooldown--;
            }
        }

        @Override
        public void stop() {
            this.castTimer = 0;
            this.activeSpell = null;
        }
    }
}
