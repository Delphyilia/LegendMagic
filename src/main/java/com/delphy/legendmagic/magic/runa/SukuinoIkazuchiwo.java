package com.delphy.legendmagic.magic.runa;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.AbstractMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

public class SukuinoIkazuchiwo implements AbstractMagic {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "sukuino_ikazuchiwo");

    @Override
    public ResourceLocation getSpellId() { return SPELL_ID; }

    @Override
    public String getName() { return "救いの雷"; }

    @Override
    public String getChant() { return "点在する神に請う。憐れな我らに救いの雷を"; }

    @Override
    public String getDescription() { return "天から真実の雷を呼び寄せる。"; }

    // 詠唱時間を3秒（60tick）に設定
    @Override
    public int getCastTime() { return 60; }

    @Override
    public void execute(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double range = 25.0; // 射程を少し長めに設定

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);

        // --- 1. エンティティ（敵）を優先的に探すレイキャスト ---
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, start, end,
                player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D),
                (e) -> !e.isSpectator() && e.isPickable(),
                range * range
        );

        Vec3 targetPos;

        if (entityHit != null) {
            // 敵に当たった場合はその足元
            targetPos = entityHit.getEntity().position();
        } else {
            // 敵がいない場合は、視線の先のブロックを探す
            HitResult blockHit = player.pick(range, 0.0F, false);
            targetPos = blockHit.getLocation();
        }

        // --- 2. 雷を召喚 ---
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(targetPos);
            bolt.setCause(player);
            level.addFreshEntity(bolt);
        }
    }
}