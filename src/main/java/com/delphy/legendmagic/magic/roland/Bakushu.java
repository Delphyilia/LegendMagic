package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 求めるは光陣>>>縛呪<ばくしゅ> (Bakushu)
 * 魔方陣から光の縄を射出し、標的を完全に捕縛する。
 * 対象を約6秒間、完全行動停止（完全スタン＋移動・跳躍・攻撃停止）にし、
 * 「紅蓮」や「倒地」「浮凍」などの大技を確実に全弾直撃させるコンボ起点となる。
 */
public class Bakushu implements Spell {

    public static final ResourceLocation ID = new ResourceLocation(LegendMagic.MODID, "bakushu");
    public static final String TAG_BOUND = "LegendMagic_BoundTimer";

    @Override
    public ResourceLocation getSpellId() {
        return ID;
    }

    @Override
    public String getName() {
        return "縛呪<ばくしゅ>";
    }

    @Override
    public String getChant() {
        return "求めるは光陣>>>縛呪<ばくしゅ>";
    }

    @Override
    public String getDescription() {
        return "光の魔方陣より神聖な縄を射出し、標的を完全スタン拘束する。大技コンボの起点となる。";
    }

    @Override
    public int getCastTime() {
        return 25; // 1.25秒
    }

    @Override
    public int getManaCost() {
        return 25;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        double range = 25.0D;

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 2.0F, 1.6F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 2.0F, 1.2F);

        // 光の縄の射出軌跡パーティクル
        int steps = (int) (range * 3);
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = start.add(look.scale(progress * range));
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);
            level.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0.05, 0.05, 0.05, 0.02);
        }

        AABB hitArea = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitArea,
                e -> e != caster && e.isAlive());

        boolean hasEye = (caster instanceof Player p && EyeUtil.hasAlphaStigma(p));
        int stunDuration = hasEye ? 160 : 120; // 6秒〜8秒スタン

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(start);
            if (toTarget.normalize().dot(look) < 0.75D) continue;

            // 完全スタン効果（超高レベル鈍足＋跳躍不能＋弱体化）
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stunDuration, 9, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, stunDuration, 200, false, false)); // 200で跳躍完全封殺
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, stunDuration, 5, false, false));
            target.setDeltaMovement(0, 0, 0);

            if (target instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.setTarget(null); // 一時的に攻撃思考リセット
            }

            // 微弱な拘束縛りダメージ
            target.hurt(caster.damageSources().magic(), 4.0F);

            // 光の縄が螺旋状に巻き付く演出
            for (int ring = 0; ring < 4; ring++) {
                double ry = target.getY() + (ring * 0.5D);
                for (int pt = 0; pt < 8; pt++) {
                    double theta = pt * (Math.PI / 4.0D);
                    level.sendParticles(ParticleTypes.END_ROD,
                            target.getX() + Math.cos(theta) * 0.7D,
                            ry,
                            target.getZ() + Math.sin(theta) * 0.7D,
                            1, 0, 0, 0, 0);
                }
            }

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.8F, 1.5F);
            break; // 単体確実拘束
        }

        announceChant(caster);
    }
}
