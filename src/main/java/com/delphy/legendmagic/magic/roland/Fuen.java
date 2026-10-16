package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.util.EyeUtil;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 求めるは氷神>>>浮凍<フエン> (Fuen)
 * 無数の氷の刃を宙に生成し、前方へ高速射出する。
 * 対象が「崩水<みすみ>」によって水濡れ状態、あるいは水中にいる場合、
 * 氷刃が瞬時に結晶化炸裂を起こして【完全凍結（移動不可）】＆ダメージ2.5倍の連携コンボを発生させる。
 */
public class Fuen implements Spell {

    public static final ResourceLocation ID = new ResourceLocation(LegendMagic.MODID, "fuen");

    @Override
    public ResourceLocation getSpellId() {
        return ID;
    }

    @Override
    public String getName() {
        return "浮凍<フエン>";
    }

    @Override
    public String getChant() {
        return "求めるは氷神>>>浮凍<フエン>";
    }

    @Override
    public String getDescription() {
        return "鋭利な氷の刃を複数射出する。崩水によって水に濡れた敵を瞬時に凍結拘束し、致命的な爆発ダメージを与える。";
    }

    @Override
    public int getCastTime() {
        return 35; // 1.75秒
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
    public void onCastingTick(Player player) {
        if (player.level().isClientSide) {
            double angle = System.currentTimeMillis() / 70.0D;
            player.level().addParticle(ParticleTypes.SNOWFLAKE,
                    player.getX() + Math.cos(angle) * 1.3D,
                    player.getY() + 1.0D,
                    player.getZ() + Math.sin(angle) * 1.3D,
                    0, 0.02, 0);
        }
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        double range = 26.0D;

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 1.8F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 2.0F, 0.8F);

        // 氷刃の軌跡パーティクル
        int steps = (int) (range * 3);
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = start.add(look.scale(progress * range));
            level.sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 4, 0.15, 0.15, 0.15, 0.02);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                    pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.05);
        }

        AABB hitArea = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitArea,
                e -> e != caster && e.isAlive());

        boolean hasEye = (caster instanceof Player p && EyeUtil.hasAlphaStigma(p));

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(start);
            if (toTarget.normalize().dot(look) < 0.75D) continue;

            float baseDamage = 14.0F;

            // ⭐ 崩水（水濡れ）または水中とのコンボ判定
            boolean isWet = target.getPersistentData().contains(Misumi.TAG_WET)
                    || target.isInWaterRainOrBubble();

            if (isWet) {
                // コンボ成立: ダメージ2.5倍 (35ダメージ)
                baseDamage *= 2.5F;
                target.getPersistentData().remove(Misumi.TAG_WET); // 水濡れ消費

                // 完全凍結（移動不可・跳躍不可 120 ticks = 6秒間）
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 9, false, true));
                target.setTicksFrozen(300); // バニラ凍結エフェクト

                // 氷破砕のド派手な演出
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                        target.getX(), target.getY() + 1.0D, target.getZ(), 50, 0.5D, 0.8D, 0.5D, 0.2D);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.5F, 0.6F);
            } else {
                // 通常時: 鈍足付与 (4秒)
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
            }

            if (hasEye) {
                baseDamage *= 2.0F;
            }

            target.hurt(caster.damageSources().freeze(), baseDamage);
        }

        announceChant(caster);
    }
}
