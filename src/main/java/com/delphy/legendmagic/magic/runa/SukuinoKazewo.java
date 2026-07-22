package com.delphy.legendmagic.magic.runa;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class SukuinoKazewo implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "sukuino_kazewo");

    @Override
    public ResourceLocation getSpellId() { return SPELL_ID; }

    @Override
    public String getName() { return "救いの風"; }

    @Override
    public String getChant() { return "天在する神に請う。憐れな我らに、救いの風を"; }

    @Override
    public String getDescription() { return "下から吹き上げる風を呼び寄せ、低速落下を可能にする。"; }

    @Override
    public int getCastTime() { return 60; } // 3秒

    @Override
    public void execute(LivingEntity caster) {
        if (!caster.level().isClientSide() && caster.level() instanceof ServerLevel serverLevel) {
            // 低速落下のポーション効果を付与 (600 tick = 30秒)
            caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0, false, true));

            // 足元に風のパーティクルを発生させる
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    caster.getX(), caster.getY(), caster.getZ(),
                    30, // パーティクルの数
                    0.5, 0.2, 0.5, // 拡散範囲
                    0.1 // スピード
            );
            serverLevel.sendParticles(ParticleTypes.SPIT,
                    caster.getX(), caster.getY(), caster.getZ(),
                    15,
                    0.5, 0.2, 0.5,
                    0.1
            );
        }
    }
}