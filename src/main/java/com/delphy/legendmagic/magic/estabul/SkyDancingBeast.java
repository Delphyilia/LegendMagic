package com.delphy.legendmagic.magic.estabul;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * 我・契約文を捧げ・天空を踊る光の魔獣を放つ
 * 直線に飛ぶローランドのIzuchiに対し、天空へ駆け上がってから遮蔽物を迂回して敵を自動追尾急降下する誘導爆撃魔法。
 */
public class SkyDancingBeast implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "sky_dancing_beast");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "天空の魔獣";
    }

    @Override
    public String getChant() {
        return "我・契約文を捧げ・天空を踊る光の魔獣を放つ";
    }

    @Override
    public String getDescription() {
        return "光の魔獣が天空を踊るように舞い上がり、遮蔽物の裏に潜む敵を自動追尾して頭上から急降下爆撃する。";
    }

    @Override
    public int getCastTime() {
        return 35; // 1.75秒 (35 ticks)
    }

    @Override
    public int getManaCost() {
        return 30;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ESTABUL;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        // 追尾対象の検索 (視線方向または周囲35m内の最も近い敵)
        double range = 35.0D;
        AABB searchBox = caster.getBoundingBox().inflate(range);
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                (e) -> e != caster && e.isAlive() && !e.isAlliedTo(caster));

        LivingEntity target = enemies.stream()
                .min(Comparator.comparingDouble((e) -> e.distanceToSqr(caster)))
                .orElse(null);

        Vec3 start = caster.getEyePosition();
        Vec3 targetPos;

        if (target != null) {
            targetPos = target.position().add(0, target.getBbHeight() * 0.5D, 0);
        } else {
            // 対象がいない場合は視線先の空中
            targetPos = start.add(caster.getLookAngle().scale(range));
        }

        float damage = 14.0F;
        if (caster instanceof Player player && EyeUtil.hasAlphaStigma(player)) {
            damage *= 2.0F; // 複写眼で28ダメージ
        }

        // 天空へ舞い上がる経由点 (Bezier曲線のような弧)
        Vec3 midSky = start.add(targetPos).scale(0.5D).add(0, 10.0D, 0);

        // 1. 上昇〜旋回〜急降下の光跡を描画
        int steps = 40;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            // 2次ベジェ曲線
            double x = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * midSky.x + t * t * targetPos.x;
            double y = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * midSky.y + t * t * targetPos.y;
            double z = (1 - t) * (1 - t) * start.z + 2 * (1 - t) * t * midSky.z + t * t * targetPos.z;

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
            }
        }

        // 飛翔音
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.PHANTOM_SWOOP, SoundSource.PLAYERS, 1.2F, 1.4F);

        // 2. 着弾時の爆撃ダメージと衝撃波
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, targetPos.x, targetPos.y, targetPos.z, 40, 1.0D, 1.0D, 1.0D, 0.15D);
        level.sendParticles(ParticleTypes.EXPLOSION, targetPos.x, targetPos.y, targetPos.z, 2, 0.5D, 0.5D, 0.5D, 0.1D);

        level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F, 1.2F);

        // 着弾地点周囲3mの全敵にスプラッシュダメージ
        AABB blastArea = new AABB(targetPos.x - 3.0D, targetPos.y - 2.0D, targetPos.z - 3.0D,
                targetPos.x + 3.0D, targetPos.y + 2.0D, targetPos.z + 3.0D);

        List<LivingEntity> hitEntities = level.getEntitiesOfClass(LivingEntity.class, blastArea, (e) -> e != caster && e.isAlive());
        for (LivingEntity hit : hitEntities) {
            hit.hurt(caster.damageSources().magic(), damage);
            // 爆風ノックバック
            Vec3 knockback = hit.position().subtract(targetPos).normalize().scale(0.6D);
            hit.push(knockback.x, 0.3D, knockback.z);
        }
    }
}
