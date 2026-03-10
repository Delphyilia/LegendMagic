package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.AbstractMagic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;


public class Izuchi implements AbstractMagic {

    // 魔法のIDを定義
    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "izuchi");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() { return "ライトニング"; }

    @Override
    public String getChant() { return "求めるは雷鳴>>>稲光<いづち>"; }

    @Override
    public String getDescription() { return "視線の先の敵に直接ダメージを与え、青い火花を散らす。"; }

    @Override
    public void execute(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // 1. レイキャストの設定
        double range = 30.0;
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);

        // 判定用のバウンディングボックス（プレイヤーの周囲を射程分広げる）
        AABB searchArea = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);

        // ⭐ 修正ポイント：ProjectileUtil.getEntityHitResult を使用し、判定を安定させる
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                searchArea,
                (entity) -> !entity.isSpectator() && entity.isPickable(),
                range * range // ここは距離の2乗を渡す仕様の場合があります
        );

        // 終点の決定
        Vec3 actualEnd = (entityHit != null) ? entityHit.getLocation() : end;

        // 2. ダメージ処理
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            livingTarget.hurt(player.damageSources().magic(), 10.0F);

            // 着弾エフェクト
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    livingTarget.getX(), livingTarget.getY() + 1, livingTarget.getZ(),
                    20, 0.2, 0.5, 0.2, 0.1);
        }

        // 3. 見た目の描画
        spawnElectricTrail(level, start, actualEnd);
    }


    private void spawnElectricTrail(ServerLevel level, Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 2);

        for (int i = 0; i < particleCount; i++) {
            double ratio = (double) i / particleCount;
            double x = start.x + (end.x - start.x) * ratio;
            double y = start.y + (end.y - start.y) * ratio;
            double z = start.z + (end.z - start.z) * ratio;

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}