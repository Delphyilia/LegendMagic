package com.delphy.legendmagic.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DarkWolfEntity extends Wolf {

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 6000; // 5分 = 5 * 60 * 20 ticks

    public DarkWolfEntity(EntityType<? extends Wolf> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    /**
     * 召喚者（プレイヤー等）の情報をもとにステータスを動的スケーリングする。
     * 固定基礎値（HP 40, 攻撃力 8）または召喚者ステータス比例値の大きい方を採用。
     */
    public void setSummoner(LivingEntity summoner) {
        if (summoner instanceof Player player) {
            this.tame(player);
        }
        this.setOrderedToSit(false);

        double summonerHp = summoner.getMaxHealth();
        double baseHp = 40.0D;
        double finalHp = Math.max(baseHp, summonerHp * 1.5D);

        double summonerAtk = summoner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double baseAtk = 8.0D;
        double finalAtk = Math.max(baseAtk, summonerAtk * 1.2D);

        AttributeInstance maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(finalHp);
            this.setHealth((float) finalHp);
        }

        AttributeInstance attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(finalAtk);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // ウィザー無効＆ウィザーで回復
        if (source.is(DamageTypes.WITHER)) {
            this.heal(10.0F);
            return false;
        }
        // 火炎耐性
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity livingTarget) {
            // 闇の追撃（ウィザー・盲目デバフ付与）
            livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() / 2.0D, livingTarget.getZ(),
                        15, 0.3D, 0.3D, 0.3D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() / 2.0D, livingTarget.getZ(),
                        10, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }
        return hurt;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // クライアント側で闇煙・ソウルフレイムパーティクルをまとう
            if (this.random.nextFloat() < 0.3F) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth();
                double py = this.getY() + this.random.nextDouble() * this.getBbHeight();
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth();
                this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.01D, 0.0D);
            }
        } else {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                despawnWithParticles();
            }
        }
    }

    private void despawnWithParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
        }
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Lifetime", this.lifetime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Lifetime")) {
            this.lifetime = tag.getInt("Lifetime");
        }
    }
}
