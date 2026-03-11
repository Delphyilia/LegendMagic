package com.delphy.legendmagic.magic.estabul;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.AbstractMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class SpiritBeast implements AbstractMagic {

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
    public void execute(LivingEntity caster) {
        int duration = 20 * 120; // 120秒

        // casterに対してエフェクトを付与
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 2));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2));

        // サーバー側の場合のみパーティクルを表示
        if (caster.level() instanceof ServerLevel serverLevel) {
            spawnParticles(serverLevel, caster);
        }
    }

    /**
     * 引数を (ServerLevel, LivingEntity) に修正
     */
    private void spawnParticles(ServerLevel level, LivingEntity entity) {
        for(int i = 0; i < 20; i++) {
            double angle = i * Math.PI * 2 / 20;
            double x = entity.getX() + Math.cos(angle) * 1.2;
            double z = entity.getZ() + Math.sin(angle) * 1.2;

            // level.sendParticles を使用
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    x, entity.getY() + 0.2, z,
                    1, 0, 0, 0, 0
            );
        }
    }
}