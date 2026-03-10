package com.delphy.legendmagic.magic.estabul;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.AbstractMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;

public class SpiritBeast implements AbstractMagic {

    // IDを定義（このIDがSpellRegistryでの登録名と一致する必要があります）
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
        return "全身の筋肉を酷使し、圧倒的な身体能力を得る。";
    }

    @Override
    public void execute(ServerPlayer player) {
        int duration = 20 * 120; // 120秒

        // 各種バフの付与
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2)); // 100は速すぎるので2~5程度が実用的です
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, 0));

        // パーティクル演出
        spawnParticles(player);
    }

    private void spawnParticles(ServerPlayer player) {
        for(int i = 0; i < 20; i++) {
            double angle = i * Math.PI * 2 / 20;
            double x = player.getX() + Math.cos(angle) * 1.2;
            double z = player.getZ() + Math.sin(angle) * 1.2;

            player.serverLevel().sendParticles(
                    ParticleTypes.ENCHANT,
                    x, player.getY() + 0.2, z,
                    1, 0, 0, 0, 0
            );
        }
    }
}