package com.delphy.legendmagic.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

public class DarkWolfEntity extends Wolf {

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 6000; // 5分 = 5 * 60 * 20 ticks

    public DarkWolfEntity(EntityType<? extends Wolf> entityType, Level level) {
        super(entityType, level);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                despawnWithParticles();
            }
        }
    }

    private void despawnWithParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
        }
        this.discard();
    }
}
