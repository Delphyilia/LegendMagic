package com.delphy.legendmagic.magic.estabul;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 我・契約文を捧げ・大気に眠る光の精獣を宿す
 * エスタブール王国の索敵・信号弾魔法。
 * 上空高く光の精獣を打ち上げ、周囲64ブロックの全Mobに発光（Glowing）を付与して壁越しに索敵する。
 */
public class LightSpiritBeast implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "light_spirit_beast");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "光の精獣";
    }

    @Override
    public String getChant() {
        return "我・契約文を捧げ・大気に眠る光の精獣を宿す";
    }

    @Override
    public String getDescription() {
        return "天高く光の精獣（信号弾）を放ち、周囲64m内の敵対生物に発光を付与して視覚化する索敵魔法。";
    }

    @Override
    public int getCastTime() {
        return 25; // 1.25秒 (25 ticks)
    }

    @Override
    public int getManaCost() {
        return 25;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ESTABUL;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        double cx = caster.getX();
        double cy = caster.getY();
        double cz = caster.getZ();

        // 1. 上空へ駆け上がる光の精獣の光柱エフェクト
        for (int y = 0; y < 35; y += 2) {
            level.sendParticles(ParticleTypes.END_ROD, cx, cy + y, cz, 4, 0.2D, 0.5D, 0.2D, 0.05D);
            level.sendParticles(ParticleTypes.GLOW, cx, cy + y, cz, 3, 0.2D, 0.2D, 0.2D, 0.02D);
        }

        // 上空での大炸裂演出
        double burstY = cy + 35.0D;
        level.sendParticles(ParticleTypes.FLASH, cx, burstY, cz, 2, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.FIREWORK, cx, burstY, cz, 60, 2.0D, 2.0D, 2.0D, 0.2D);

        level.playSound(null, cx, cy, cz, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.5F, 1.0F);
        level.playSound(null, cx, cy, cz, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);

        // 2. 半径64ブロックの全Mobに発光（Glowing）を付与 (600 tick = 30秒)
        AABB scanArea = new AABB(cx - 64.0D, cy - 32.0D, cz - 64.0D, cx + 64.0D, cy + 32.0D, cz + 64.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, scanArea, (e) -> e != caster && e.isAlive());

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false, true));
            // 発光付与時の小さな光の火花
            level.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1.0D, target.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.02D);
        }
    }
}
