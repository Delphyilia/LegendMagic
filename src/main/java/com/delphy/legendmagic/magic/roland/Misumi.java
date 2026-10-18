package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.util.EyeUtil;

import net.minecraft.core.BlockPos;
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
 * 求めるは水雲>>>崩水<みすみ> (Misumi)
 * 魔方陣の中心から高圧水流を直線噴射する。
 * 通常は水圧ノックバック用の低威力技だが、ピグリン・ブレイズ等の火炎耐性・ネザーMob相手には威力が4倍化する特効兵器。
 * また標的に「水濡れ(Wet)」タグを付与し、「浮凍<フエン>」の瞬間凍結コンボの起点となる。
 */
public class Misumi implements Spell {

    public static final ResourceLocation ID = new ResourceLocation(LegendMagic.MODID, "misumi");
    public static final String TAG_WET = "LegendMagic_WetTimer";

    @Override
    public ResourceLocation getSpellId() {
        return ID;
    }

    @Override
    public String getName() {
        return "崩水<みすみ>";
    }

    @Override
    public String getChant() {
        return "求めるは水雲>>>崩水<みすみ>";
    }

    @Override
    public String getDescription() {
        return "高圧水流を直線噴射し敵を押し流す。火炎耐性を持つMobには致命的な大ダメージを与え、浮凍の凍結コンボを誘発する。";
    }

    @Override
    public int getCastTime() {
        return 20; // 1.0秒で素早く発射可能
    }

    @Override
    public int getManaCost() {
        return 15;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 start = caster.getEyePosition();
        Vec3 look = com.delphy.legendmagic.util.MagicUtil.getSpellAimDirection(caster, start);
        double range = 24.0D;

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 2.0F, 0.9F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 2.0F, 1.4F);

        // 高圧水流パーティクル（直線ビーム）
        int steps = (int) (range * 3);
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = start.add(look.scale(progress * range));
            level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.05);
            level.sendParticles(ParticleTypes.FALLING_WATER, pos.x, pos.y, pos.z, 3, 0.1, 0.1, 0.1, 0.02);

            // 周囲の炎ブロックを瞬時に消火
            BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
            if (level.getBlockState(blockPos).is(Blocks.FIRE) || level.getBlockState(blockPos).is(Blocks.SOUL_FIRE)) {
                level.removeBlock(blockPos, false);
            }
        }

        // 判定エリア
        AABB beamBox = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, beamBox,
                e -> e != caster && e.isAlive());

        boolean hasEye = (caster instanceof Player p && EyeUtil.hasAlphaStigma(p));

        for (LivingEntity target : targets) {
            // 水流の射線判定
            Vec3 toTarget = target.position().subtract(start);
            double dot = toTarget.normalize().dot(look);
            if (dot < 0.8D) continue; // 射線外は除外

            // 基本ダメージ: 5 (低威力)
            float damage = 5.0F;

            // ⭐ 火炎耐性持ちMobへの特効判定（ブレイズ・ピグリン・マグマキューブ等）
            boolean isFireImmune = target.fireImmune()
                    || target.hasEffect(MobEffects.FIRE_RESISTANCE)
                    || target.getType().getDescriptionId().contains("blaze")
                    || target.getType().getDescriptionId().contains("piglin");

            if (isFireImmune) {
                damage = 24.0F; // 特効により大ダメージ（約5倍）
                level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 1.0D, target.getZ(),
                        30, 0.4D, 0.6D, 0.4D, 0.08D);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.5F, 0.8F);
            }

            if (hasEye) {
                damage *= 2.0F;
            }

            target.hurt(caster.damageSources().magic(), damage);

            // 水圧ノックバック（進行方向へ強く押し流す）
            target.knockback(1.5D, -look.x, -look.z);

            // 炎上中の敵の消火
            target.clearFire();

            // ⭐ 「水濡れ」状態の付与（200 ticks = 10秒間、浮凍コンボ用）
            target.getPersistentData().putInt(TAG_WET, 200);

            // 水しぶきエフェクト
            level.sendParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getY() + 1.0D, target.getZ(),
                    25, 0.5D, 0.5D, 0.5D, 0.1D);
        }

        announceChant(caster);
    }
}
