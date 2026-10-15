package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 求めるは震牙>>>倒地<ちがしら>
 * 地面の下から巨大な岩石杭を突き出して敵を串刺し・打ち上げるローランド流の地脈物理破壊術。
 * 地中発動の物理現象であるため、大気中魔力を喰らう「殲滅眼（イーノ・ドゥーエ）」でも防ぐことができない。
 */
public class Chigashira implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "chigashira");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "倒地";
    }

    @Override
    public String getChant() {
        return "求めるは震牙>>>倒地<ちがしら>";
    }

    @Override
    public String getDescription() {
        return "地盤を激しく震わせ、地中から連続して岩石杭を突き上げて敵を串刺し・打ち上げる。地中攻撃のため殲滅眼でも吸収不可。";
    }

    @Override
    public int getCastTime() {
        return 50; // 2.5秒 (50 ticks)
    }

    @Override
    public int getManaCost() {
        return 35;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void onCastingTick(Player player) {
        // 詠唱中の地鳴り演出
        if (player.level().random.nextFloat() < 0.35F) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D;
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0D;
            double y = player.getY() + 0.1D;

            player.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()),
                    x, y, z, 0, 0.15D, 0);
        }
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 look = caster.getLookAngle();
        Vec3 start = caster.position();

        float damage = 18.0F;
        if (caster instanceof Player player && EyeUtil.hasAlphaStigma(player)) {
            damage *= 2.0F; // 複写眼で36ダメージ
        }

        // 前方へ向けて直線状に6本の地中杭を連続噴出
        int spikeCount = 7;
        double spacing = 2.5D;

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, 0.6F);

        for (int i = 1; i <= spikeCount; i++) {
            double targetX = start.x + look.x * (i * spacing);
            double targetZ = start.z + look.z * (i * spacing);

            // 地表面の高さを取得
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos((int) targetX, (int) caster.getY(), (int) targetZ));

            double groundY = surfacePos.getY();

            // イヴォーカーファングの設置 (杭の見た目と接触判定)
            EvokerFangs fangs = new EvokerFangs(level, targetX, groundY, targetZ, caster.getYRot(), i * 2, caster);
            level.addFreshEntity(fangs);

            // 岩石破片パーティクルと地鳴り音
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    targetX, groundY + 0.5D, targetZ, 25, 0.4D, 0.8D, 0.4D, 0.2D);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    targetX, groundY + 0.5D, targetZ, 1, 0, 0, 0, 0);

            // 範囲内の敵に対する物理ノックアップ（上空打ち上げ）＆大ダメージ
            AABB hitBox = new AABB(targetX - 1.5D, groundY - 0.5D, targetZ - 1.5D,
                    targetX + 1.5D, groundY + 2.5D, targetZ + 1.5D);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox, (e) -> e != caster && e.isAlive());
            for (LivingEntity target : targets) {
                // 物理ダメージ（地中攻撃）
                target.hurt(caster.damageSources().generic(), damage);

                // 上空へ強烈にノックアップ
                target.setDeltaMovement(target.getDeltaMovement().x, 0.85D, target.getDeltaMovement().z);
                target.hasImpulse = true;
            }
        }
    }
}
