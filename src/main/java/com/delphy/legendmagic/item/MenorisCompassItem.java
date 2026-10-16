package com.delphy.legendmagic.item;

import com.delphy.legendmagic.LegendMagic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * メノリスの羅針盤（Menoris Compass）
 * 伝勇伝の主要舞台である「メノリス大陸」と「現実世界（オーバーワールド）」を自由に行き来する次元移動アイテム。
 * 既存ワールドにModを導入した際にも、未生成のメノリス大陸で各国構造物（ローランド・エスタブール・ルーナ）の探索を可能にする。
 */
public class MenorisCompassItem extends Item {

    public static final ResourceKey<Level> MENORIS_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(LegendMagic.MODID, "menoris_continent"));

    public MenorisCompassItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE).durability(0));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel currentLevel = serverPlayer.serverLevel();
            boolean inMenoris = currentLevel.dimension().equals(MENORIS_DIMENSION);

            ServerLevel targetLevel;
            BlockPos targetPos;

            if (inMenoris) {
                // メノリス大陸からオーバーワールドへ帰還
                targetLevel = serverPlayer.server.getLevel(Level.OVERWORLD);
                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                targetPos = (respawnPos != null) ? respawnPos : targetLevel.getSharedSpawnPos();
                targetPos = findSafeSurfacePos(targetLevel, targetPos);
                serverPlayer.displayClientMessage(
                        Component.literal("羅針盤の導きにより、現実世界へ帰還した。").withStyle(ChatFormatting.AQUA), true);
            } else {
                // オーバーワールド等からメノリス大陸へ突入
                targetLevel = serverPlayer.server.getLevel(MENORIS_DIMENSION);
                if (targetLevel == null) {
                    serverPlayer.displayClientMessage(
                            Component.literal("§cメノリス大陸への次元通路が確立されていません。"), true);
                    return InteractionResultHolder.fail(stack);
                }

                // プレイヤーの現在座標の対応地点地表を探索
                BlockPos searchPos = new BlockPos((int) serverPlayer.getX(), 64, (int) serverPlayer.getZ());
                targetPos = findSafeSurfacePos(targetLevel, searchPos);

                serverPlayer.displayClientMessage(
                        Component.literal("メノリス大陸への扉が開かれた……！").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
            }

            // 出発地点の演出（SE & パーティクル）
            currentLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.5F, 1.2F);
            currentLevel.sendParticles(ParticleTypes.ENCHANT, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                    60, 0.6D, 1.0D, 0.6D, 0.2D);

            // テレポート実行
            serverPlayer.teleportTo(targetLevel,
                    targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                    Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot());

            // 到着地点の演出
            targetLevel.playSound(null, targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.5F);
            targetLevel.sendParticles(ParticleTypes.FLASH, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);

            player.getCooldowns().addCooldown(this, 60); // 3秒クールダウン
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 安全な地表ブロック位置を探索して返す（足場が不安定な場合は足場を作成）
     */
    private BlockPos findSafeSurfacePos(ServerLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        if (surfaceY <= level.getMinBuildHeight() + 5) {
            surfaceY = 70; // 予備高度
        }

        BlockPos landingPos = new BlockPos(x, surfaceY, z);
        BlockPos floorPos = landingPos.below();

        // 足場ブロックが空気・液体なら石レンガで足場を保証
        if (level.isEmptyBlock(floorPos) || level.getBlockState(floorPos).liquid()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlockAndUpdate(floorPos.offset(dx, 0, dz), Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
        }

        // 頭上2ブロックを空気で確保（窒息防止）
        level.removeBlock(landingPos, false);
        level.removeBlock(landingPos.above(), false);

        return landingPos;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("はるか異境の大地を指し示す神秘の羅針盤。").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("右クリックで新大陸「メノリス大陸」と現実世界を往復する。").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("生成済みのワールドでも、この大陸で各国の構造物を探索できる。").withStyle(ChatFormatting.AQUA));
    }
}
