package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.entity.ai.MagicCaster;
import com.delphy.legendmagic.util.EyeUtil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 求めるは静寂>>>闇庭<あんて> (Ante)
 * 空気の振動を停止させ、周囲の「音」を完全に消去して静寂を生み出す妨害魔法。
 * 術者の周囲12mに静寂の結界（闇庭）を展開し、範囲内の敵魔導士の呪文詠唱を完全に遮断・沈黙（Silence）させる。
 * 対魔導士集団戦や詠唱妨害において絶大な戦術価値を持つ。
 */
public class Ante implements Spell {

    public static final ResourceLocation ID = new ResourceLocation(LegendMagic.MODID, "ante");
    public static final String TAG_SILENCED = "LegendMagic_SilencedTimer";

    @Override
    public ResourceLocation getSpellId() {
        return ID;
    }

    @Override
    public String getName() {
        return "闇庭<あんて>";
    }

    @Override
    public String getChant() {
        return "求めるは静寂>>>闇庭<あんて>";
    }

    @Override
    public String getDescription() {
        return "空気の振動を止め、周囲の音を完全に消去する静寂結界。範囲内の敵の詠唱を封じ、魔法の使用を妨害する。";
    }

    @Override
    public int getCastTime() {
        return 30; // 1.5秒
    }

    @Override
    public int getManaCost() {
        return 30;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        double radius = 12.0D;
        AABB silenceArea = caster.getBoundingBox().inflate(radius);

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.5F, 0.5F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 1.2F);

        // 静寂の結界（ドーム状パーティクル）
        for (int i = 0; i < 40; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2.0D;
            double phi = level.random.nextDouble() * Math.PI;
            double px = caster.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double py = caster.getY() + radius * Math.cos(phi);
            double pz = caster.getZ() + radius * Math.sin(phi) * Math.sin(theta);

            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, px, py, pz, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.ASH, px, py, pz, 1, 0, 0, 0, 0);
        }

        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, silenceArea,
                e -> e != caster && e.isAlive());

        boolean hasEye = (caster instanceof Player p && EyeUtil.hasAlphaStigma(p));
        int silenceDuration = hasEye ? 160 : 120; // 6秒〜8秒沈黙

        for (LivingEntity enemy : enemies) {
            // 沈黙タグ付与
            enemy.getPersistentData().putInt(TAG_SILENCED, silenceDuration);

            // 敵MobのAIターゲット思考とナビゲーションを停止（混乱・沈黙）
            if (enemy instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }

            // 弱体化・暗闇
            enemy.addEffect(new MobEffectInstance(MobEffects.DARKNESS, silenceDuration, 0, false, false));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, silenceDuration, 1, false, false));

            // 音消去の微小ダメージ
            enemy.hurt(caster.damageSources().magic(), 6.0F);

            level.sendParticles(ParticleTypes.SONIC_BOOM, enemy.getX(), enemy.getY() + 1.0D, enemy.getZ(),
                    1, 0, 0, 0, 0);
        }

        announceChant(caster);
    }
}
