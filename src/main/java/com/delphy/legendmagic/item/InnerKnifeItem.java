package com.delphy.legendmagic.item;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.block.ModBlocks;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * キーアイテム「内面のナイフ」
 * 空間を切り裂き、異次元の「魔法研究所」へプレイヤーを転移させる。
 * 研究所の「運命の祭壇」に使用することで、「運命の破壊者」との再戦を果たすことができる。
 */
public class InnerKnifeItem extends Item {

    public static final ResourceKey<Level> LAB_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(LegendMagic.MODID, "magic_laboratory"));

    public InnerKnifeItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC).durability(0));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ServerLevel currentLevel = serverPlayer.serverLevel();
            boolean inLab = currentLevel.dimension().equals(LAB_DIMENSION);

            ServerLevel targetLevel;
            BlockPos targetPos;

            if (inLab) {
                // 研究所からオーバーワールドへ帰還
                targetLevel = serverPlayer.server.getLevel(Level.OVERWORLD);
                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                targetPos = (respawnPos != null) ? respawnPos : targetLevel.getSharedSpawnPos();
                serverPlayer.displayClientMessage(Component.literal("空間の裂け目を通って現実世界へ帰還した。").withStyle(ChatFormatting.DARK_PURPLE), true);
            } else {
                // オーバーワールド等から魔法研究所へ突入
                targetLevel = serverPlayer.server.getLevel(LAB_DIMENSION);
                if (targetLevel == null) {
                    serverPlayer.displayClientMessage(Component.literal("§c魔法研究所への次元通路が不安定です。"), true);
                    return InteractionResultHolder.fail(stack);
                }
                targetPos = new BlockPos(0, 32, 0);

                // 研究所のスポーン地点周囲に研究室の基礎空間（31x31）を保証
                ensureLabPlatform(targetLevel, targetPos);

                serverPlayer.displayClientMessage(Component.literal("内面のナイフで空間を切り裂き、魔法研究所へ突入した……！").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true);
            }

            // 空間裂断エフェクト＆音
            currentLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0F, 0.5F);
            currentLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.5F, 1.8F);
            currentLevel.sendParticles(ParticleTypes.PORTAL, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                    50, 0.5D, 1.0D, 0.5D, 0.2D);

            // テレポート実行
            serverPlayer.teleportTo(targetLevel,
                    targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                    Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot());

            targetLevel.playSound(null, targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0F, 0.5F);
            targetLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D,
                    50, 0.5D, 1.0D, 0.5D, 0.1D);

            player.getCooldowns().addCooldown(this, 60); // 3秒クールダウン
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 研究所到着地点の床と運命の祭壇の生成保証
     */
    private void ensureLabPlatform(ServerLevel level, BlockPos center) {
        int radius = 12;
        int y = center.getY() - 1; // Y=31 (床面)

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos floorPos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                if (level.isEmptyBlock(floorPos) || !level.getBlockState(floorPos).isSolid()) {
                    level.setBlockAndUpdate(floorPos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                }
                // 上空の空気ブロックを確保
                for (int h = 1; h <= 8; h++) {
                    BlockPos airPos = floorPos.above(h);
                    if (!level.isEmptyBlock(airPos) && level.getBlockState(airPos).getBlock() != ModBlocks.ALTAR_OF_FATE.get()) {
                        level.removeBlock(airPos, false);
                    }
                }
            }
        }

        // 中央奥（Z=+6）に「運命の祭壇」が未設置なら設置
        BlockPos altarPos = center.offset(0, 0, 6);
        if (level.getBlockState(altarPos).getBlock() != ModBlocks.ALTAR_OF_FATE.get()) {
            level.setBlockAndUpdate(altarPos, ModBlocks.ALTAR_OF_FATE.get().defaultBlockState());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("空間の境界を切り裂く特異な短剣。").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("右クリックで専用ディメンション「魔法研究所」へ転移する。").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("最奥の「運命の祭壇」に使用することで、何度でも決戦に臨める。").withStyle(ChatFormatting.GOLD));
    }
}
