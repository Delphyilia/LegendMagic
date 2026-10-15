package com.delphy.legendmagic.magic.estabul;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 我・契約文を捧げ・宙を覆う精霊の力を放つ
 * エスタブール王国の最高峰空間制御魔法。
 * 目標地点に持続する青い渦（Vortex）を展開し、周囲の敵や矢・投擲物を中心へ猛烈に引き寄せて拘束し、継続的な斬撃ダメージを与え続ける。
 */
public class VortexOfSpirits implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "vortex_of_spirits");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "青い渦";
    }

    @Override
    public String getChant() {
        return "我・契約文を捧げ・宙を覆う精霊の力を放つ";
    }

    @Override
    public String getDescription() {
        return "空間に持続する青い渦を展開し、敵や飛来する矢をブラックホールのように吸引・拘束してスライスダメージを与える。";
    }

    @Override
    public int getCastTime() {
        return 60; // 3.0秒 (60 ticks)
    }

    @Override
    public int getManaCost() {
        return 60;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ESTABUL;
    }

    @Override
    public void onCastingTick(Player player) {
        // 詠唱中に足元に蒼い精霊の力学渦粒子
        if (player.level().random.nextFloat() < 0.4F) {
            double angle = player.level().random.nextDouble() * Math.PI * 2.0D;
            double r = 1.0D + player.level().random.nextDouble() * 2.0D;
            double x = player.getX() + r * Math.cos(angle);
            double z = player.getZ() + r * Math.sin(angle);
            double y = player.getY() + 0.1D;

            player.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.05D, 0);
        }
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        double maxDist = 30.0D;
        Vec3 eyePos = caster.getEyePosition();
        Vec3 lookVec = caster.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(maxDist));

        // 視線先の地面を検出
        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));

        Vec3 targetPos;
        if (hitResult.getType() != HitResult.Type.MISS) {
            targetPos = hitResult.getLocation();
        } else {
            // 地面に当たらない場合は視線先20mの空中
            targetPos = eyePos.add(lookVec.scale(20.0D));
        }

        // ダメージ計算（0.5秒ごとのスライスダメージ）
        float sliceDamage = 4.0F;
        if (caster instanceof Player player && EyeUtil.hasAlphaStigma(player)) {
            sliceDamage *= 2.0F; // 複写眼で8.0Fダメージ
        }

        // 8秒間（160 ticks）持続する渦を生成
        int durationTicks = 160;
        VortexManager.spawnVortex(level, targetPos, caster, sliceDamage, durationTicks);
    }
}
