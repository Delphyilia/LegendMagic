package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.item.LegendMagicItems;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.jetbrains.annotations.Nullable;

/**
 * ガスターク兵（Gastark Soldier）
 * 大陸全土で忘却欠片（遺物）を蒐集し、自ら兵器として武装する軍事超大国ガスタークのエリート重装兵。
 * スポーン時に「雷獣の指輪」または「黒叡の指輪」のいずれかを必ず装備・行使する。
 * - 雷獣型: 高速移動＆攻撃時感電追撃＋オオカミ召喚
 * - 黒叡型: 重打撃＆攻撃時暗黒追撃＋オオカミ召喚
 * 撃破時に「遺物の欠片（Relic Shard）」を確定ドロップし、稀に所持している指輪本体をそのままドロップする。
 */
public class GastarkSoldierEntity extends Monster {

    public static final int RELIC_THUNDER = 0; // 雷獣の指輪
    public static final int RELIC_DARK = 1;    // 黒叡の指輪

    private static final EntityDataAccessor<Integer> RELIC_TYPE =
            SynchedEntityData.defineId(GastarkSoldierEntity.class, EntityDataSerializers.INT);

    private int summonCooldown = 60; // 初回召喚までの待機時間

    public GastarkSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RELIC_TYPE, RELIC_THUNDER);
    }

    public int getRelicType() {
        return this.entityData.get(RELIC_TYPE);
    }

    public void setRelicType(int type) {
        this.entityData.set(RELIC_TYPE, type);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D) // 一般兵を凌駕するタフネス
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);

        // 50%で雷獣の指輪、50%で黒叡の指輪を保持
        int assignedType = this.random.nextBoolean() ? RELIC_THUNDER : RELIC_DARK;
        this.setRelicType(assignedType);

        // 装備セット
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));

        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // 遺物ごとの常時エフェクト
        if (this.level().isClientSide) {
            if (this.getRelicType() == RELIC_THUNDER && this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                        0.0D, 0.05D, 0.0D);
            } else if (this.getRelicType() == RELIC_DARK && this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                        0.0D, 0.03D, 0.0D);
            }
        } else {
            // 戦闘時の遺物行使（オオカミ召喚）
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (this.summonCooldown > 0) {
                    this.summonCooldown--;
                } else {
                    executeRelicSummon();
                    this.summonCooldown = 600; // 30秒CT
                }
            }
        }
    }

    /**
     * 保持している忘却欠片の固有召喚を発動
     */
    private void executeRelicSummon() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.getRelicType() == RELIC_THUNDER) {
            // 雷獣召喚
            ThunderWolfEntity wolf = new ThunderWolfEntity(ModEntities.THUNDER_WOLF.get(), serverLevel);
            wolf.moveTo(this.getX() + 1.5D, this.getY(), this.getZ() + 1.5D, this.getYRot(), 0.0F);
            wolf.setOwnerUUID(this.getUUID());
            wolf.setTarget(this.getTarget());
            serverLevel.addFreshEntity(wolf);

            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.0F, 1.5F);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY() + 1.0D, this.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.1D);
        } else {
            // 黒叡召喚
            DarkWolfEntity wolf = new DarkWolfEntity(ModEntities.DARK_WOLF.get(), serverLevel);
            wolf.moveTo(this.getX() - 1.5D, this.getY(), this.getZ() - 1.5D, this.getYRot(), 0.0F);
            wolf.setOwnerUUID(this.getUUID());
            wolf.setTarget(this.getTarget());
            serverLevel.addFreshEntity(wolf);

            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0F, 0.8F);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.0D, this.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.05D);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            if (this.getRelicType() == RELIC_THUNDER) {
                // 感電追撃＋落雷エフェクト
                living.hurt(this.damageSources().lightningBolt(), 4.0F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            living.getX(), living.getY() + 1.0D, living.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.1D);
                }
            } else {
                // 闇追撃＋ソウルファイア着火
                living.hurt(this.damageSources().wither(), 4.0F);
                living.setRemainingFireTicks(60);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL,
                            living.getX(), living.getY() + 1.0D, living.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.05D);
                }
            }
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // 遺物の欠片を確定で1〜2個ドロップ
        int shardCount = 1 + this.random.nextInt(2 + looting);
        this.spawnAtLocation(new ItemStack(LegendMagicItems.RELIC_SHARD.get(), shardCount));

        // 稀に所持している忘却欠片（指輪本体）をドロップ (通常10%, ルーティングで増加)
        float dropChance = 0.10F + (looting * 0.05F);
        if (this.random.nextFloat() < dropChance) {
            ItemStack relicStack = (this.getRelicType() == RELIC_THUNDER)
                    ? new ItemStack(LegendMagicItems.RING_OF_THUNDER_BEASTS.get())
                    : new ItemStack(LegendMagicItems.RING_OF_THE_DARK_EMPEROR.get());
            this.spawnAtLocation(relicStack);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("RelicType", this.getRelicType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("RelicType")) {
            this.setRelicType(compound.getInt("RelicType"));
        }
    }
}
