package com.delphy.legendmagic.magic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
public class SelfStrengtheningSpell {

    public static final String CHANT = "我・契約文を捧げ・大地に眠る悪意の精獣を宿す";

    public static void cast(ServerPlayer player) {

        int duration = 20 * 20; // 20秒
        int amplifier = 5; // レベル2相当

        // 移動速度UP
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                duration,
                10
        ));

        // 攻撃力UP
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                duration,
                amplifier
        ));

        // 防御UP
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                duration,
                amplifier
        ));

        // ⭐ ここにパーティクル
        for(int i = 0; i < 20; i++) {

            double angle = i * Math.PI * 2 / 20;

            double x = player.getX() + Math.cos(angle) * 1.2;
            double z = player.getZ() + Math.sin(angle) * 1.2;

            player.serverLevel().sendParticles(
                    ParticleTypes.ENCHANT,
                    x,
                    player.getY() + 0.2,
                    z,
                    1,
                    0,0,0,
                    0
            );
        }
    }
}
