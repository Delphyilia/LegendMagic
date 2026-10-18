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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * ローランド上位禁呪「稲死光<いづち>」
 * 完全無詠唱・無陣・即発動。
 * 術者の頭上に横向きの水平な雷（横雷）を瞬時に物質化させ、
 * 視線方向の対象Mobへ超高速で射出して消滅級の雷撃ダメージを与える。
 */
public class IzuchiDeath implements Spell {

    public static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "izuchi_death");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "稲死光";
    }

    @Override
    public String getChant() {
        return "稲死光<いづち>";
    }

    @Override
    public String getDescription() {
        return "ローランド最高峰の禁呪。完全無詠唱・即発動で頭上に横雷を現出させ、超高速で射出する必殺の雷撃。";
    }

    @Override
    public int getCastTime() {
        return 0; // 完全無詠唱・0 tick 即発動
    }

    @Override
    public int getManaCost() {
        return 40;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        double range = 48.0D;

        float damage = calculateDamage(caster);

        // 術者の頭上（横雷の発生源）
        Vec3 headPos = caster.getEyePosition().add(0, 0.7D, 0);
        Vec3 look = com.delphy.legendmagic.util.MagicUtil.getSpellAimDirection(caster, headPos);
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 0.001D) {
            right = new Vec3(1, 0, 0);
        }

        // 1. 頭上に水平な横雷（幅3ブロックの電気スパークと爆光）を展開
        for (double w = -1.8D; w <= 1.8D; w += 0.2D) {
            Vec3 p = headPos.add(right.scale(w));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 4, 0.05D, 0.05D, 0.05D, 0.02D);
            level.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 2, 0.02D, 0.02D, 0.02D, 0.01D);
        }

        // 2. ターゲットの特定（レイキャスト）
        Vec3 end = headPos.add(look.scale(range));
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                caster, headPos, end,
                caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D),
                (e) -> !e.isSpectator() && e.isPickable(),
                range * range
        );

        Vec3 actualEnd = (entityHit != null) ? entityHit.getLocation() : end;

        // 3. 超高速水平雷撃の弾道パーティクル（頭上から目標地点へ）
        int steps = (int) (headPos.distanceTo(actualEnd) * 4);
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            Vec3 point = headPos.lerp(actualEnd, progress);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 3, 0.08D, 0.08D, 0.08D, 0.05D);
            level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 2, 0.04D, 0.04D, 0.04D, 0.02D);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.FLASH, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        // 4. 着弾点での大爆発エフェクト＆雷鳴音
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.5F, 1.4F);
        level.playSound(null, actualEnd.x, actualEnd.y, actualEnd.z,
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 1.8F);

        // 演出用の落雷召喚（着弾点）
        LightningBolt visualBolt = EntityType.LIGHTNING_BOLT.create(level);
        if (visualBolt != null) {
            visualBolt.moveTo(actualEnd.x, actualEnd.y, actualEnd.z);
            visualBolt.setVisualOnly(true);
            level.addFreshEntity(visualBolt);
        }

        // 5. 直撃ダメージ適用
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            livingTarget.hurt(caster.damageSources().magic(), damage);
            // ノックバック
            livingTarget.knockback(1.2D, -look.x, -look.z);
        }

        // 詠唱文アナウンス
        announceChant(caster);
    }

    private float calculateDamage(LivingEntity caster) {
        float base = 36.0F;
        if (EyeUtil.hasAlphaStigma(caster)) {
            base *= 2.0F; // 複写眼バフ: 72ダメージ
        }
        return base;
    }
}
