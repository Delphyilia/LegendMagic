package com.delphy.legendmagic.block;

import com.delphy.legendmagic.block.entity.OfferingPedestalBlockEntity;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class OfferingPedestalBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public OfferingPedestalBlock(Properties properties) {
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
        return new OfferingPedestalBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof OfferingPedestalBlockEntity pedestal)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);
        ItemStack stored = pedestal.getStoredItem();

        if (stored.isEmpty()) {
            if (!heldItem.isEmpty()) {
                if (!level.isClientSide) {
                    ItemStack toInsert = heldItem.split(1);
                    pedestal.setStoredItem(toInsert);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
            if (!level.isClientSide) {
                ItemStack extracted = pedestal.removeStoredItem();
                if (!player.addItem(extracted)) {
                    player.drop(extracted, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OfferingPedestalBlockEntity pedestal) {
                if (!pedestal.getStoredItem().isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pedestal.getStoredItem());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
