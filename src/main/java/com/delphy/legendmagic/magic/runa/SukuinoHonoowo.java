package com.delphy.legendmagic.magic.runa;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 救いの炎を
 * ルーナ帝国の生活支援魔法。ファイヤーチャージの射出、および狙ったブロックへの着火を行う。
 * 火打ち石と打ち金やファイヤーチャージの代用となる。
 */
public class SukuinoHonoowo implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "sukuino_honoowo");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "救いの炎";
    }

    @Override
    public String getChant() {
        return "天在する神に請う。憐れな我らに、救いの炎を";
    }

    @Override
    public String getDescription() {
        return "神の慈悲の炎を呼び寄せ、ファイヤーチャージを放つ。ブロックへの着火や光源確保に便利。";
    }

    @Override
    public int getCastTime() {
        return 15; // 0.75秒 (15 ticks)
    }

    @Override
    public int getManaCost() {
        return 10;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.RUNA;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 look = caster.getLookAngle();
        Vec3 start = caster.getEyePosition().add(look.scale(1.0D));

        // 1. 小型ファイヤーチャージの射出
        SmallFireball fireball = new SmallFireball(level, caster, look.x, look.y, look.z);
        fireball.setPos(start.x, start.y, start.z);
        level.addFreshEntity(fireball);

        // 2. 直近ブロックへの着火判定（至近距離を狙っている場合）
        HitResult hit = caster.pick(5.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos targetPos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (level.isEmptyBlock(targetPos)) {
                level.setBlockAndUpdate(targetPos, BaseFireBlock.getState(level, targetPos));
            }
        }

        // 発射音と火炎パーティクル
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.FLAME, start.x, start.y, start.z, 15, 0.2D, 0.2D, 0.2D, 0.05D);
    }
}