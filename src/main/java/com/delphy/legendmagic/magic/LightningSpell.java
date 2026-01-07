package com.delphy.legendmagic.magic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

public class LightningSpell {

    public static final String CHANT =
            "求めるは雷鳴>>>稲光<いづち>";

    public static void cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // 視線の先を取得
        HitResult hit = player.pick(20.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult bhr)) return;

        BlockPos pos = bhr.getBlockPos();

        // 雷エンティティ生成
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;

        bolt.moveTo(Vec3.atBottomCenterOf(pos));
        bolt.setCause(player);

        level.addFreshEntity(bolt);
    }
}
