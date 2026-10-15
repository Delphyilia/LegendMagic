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

public class SpiritBeast implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "spirit_beast");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "自己強化";
    }

    @Override
    public String getChant() {
        return "我・契約文を捧げ・大地に眠る悪意の精獣を宿す";
    }

    @Override
    public String getDescription() {
        return "全身の筋肉を酷使し、圧倒的な身体能力を得る。移動速度II・攻撃力II・耐性IIを120秒間付与。";
    }

    @Override
    public int getCastTime() {
        return 40; // 2.0秒 (40 ticks)
    }

    @Override
    public int getManaCost() {
        return 40;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ESTABUL;
    }

    @Override
    public void execute(LivingEntity caster) {
        int duration = 20 * 120; // 120秒

        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1));

        if (caster.level() instanceof ServerLevel serverLevel) {
            spawnParticles(serverLevel, caster);
            serverLevel.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0F, 1.6F);
        }
    }

    private void spawnParticles(ServerLevel level, LivingEntity entity) {
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2.0D / 24.0D;
            double x = entity.getX() + Math.cos(angle) * 1.2D;
            double z = entity.getZ() + Math.sin(angle) * 1.2D;

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, entity.getY() + 0.3D, z, 1, 0, 0.05D, 0, 0.02D);
            level.sendParticles(ParticleTypes.ENCHANT, x, entity.getY() + 0.8D, z, 2, 0.1D, 0.2D, 0.1D, 0.1D);
        }
    }
}