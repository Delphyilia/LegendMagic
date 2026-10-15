package com.delphy.legendmagic.block;

import com.delphy.legendmagic.block.entity.ModBlockEntities;
import com.delphy.legendmagic.block.entity.RelicAltarBlockEntity;
import com.delphy.legendmagic.item.RelicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RelicAltarBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public RelicAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelicAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.RELIC_ALTAR_BE.get(), RelicAltarBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RelicAltarBlockEntity altar)) return InteractionResult.PASS;

        if (altar.isRitualActive()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("§d儀式が進行中です……"), true);
            }
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack storedRelic = altar.getStoredRelic();

        // 1. スニーク右クリック: 回収
        if (player.isShiftKeyDown()) {
            if (!storedRelic.isEmpty()) {
                if (!level.isClientSide) {
                    ItemStack extracted = altar.removeStoredRelic();
                    if (!player.addItem(extracted)) {
                        player.drop(extracted, false);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 2. 台座が空で、手にRelicItemを持っている場合: セット
        if (storedRelic.isEmpty()) {
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof RelicItem) {
                if (!level.isClientSide) {
                    ItemStack toInsert = heldItem.split(1);
                    altar.setStoredRelic(toInsert);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
            // 3. 台座にRelicItemがある状態で通常右クリック
            if (heldItem.isEmpty()) {
                // 素手で右クリック -> 儀式起動チェック
                if (altar.canStartRitual()) {
                    if (!level.isClientSide) {
                        altar.startRitual(player);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    if (!level.isClientSide) {
                        int currentLevel = RelicItem.getRelicLevel(storedRelic);
                        if (currentLevel >= RelicItem.MAX_RELIC_LEVEL) {
                            player.displayClientMessage(Component.literal("§6この忘却欠片は既に限界まで強化されています。"), true);
                        } else {
                            player.displayClientMessage(Component.literal("§c儀式を開始できません。東西南北（2m離れた位置）の4つの供物台座に「遺物の欠片」を捧げてください。"), true);
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                // 何かアイテムを持っている場合は回収
                if (!level.isClientSide) {
                    ItemStack extracted = altar.removeStoredRelic();
                    if (!player.addItem(extracted)) {
                        player.drop(extracted, false);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RelicAltarBlockEntity altar) {
                if (!altar.getStoredRelic().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), altar.getStoredRelic());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
