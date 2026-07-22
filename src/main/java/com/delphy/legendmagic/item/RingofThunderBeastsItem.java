package com.delphy.legendmagic.item;

import com.delphy.legendmagic.entity.ThunderWolfEntity;
import com.delphy.legendmagic.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RingofThunderBeastsItem extends Item {

    public RingofThunderBeastsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            
            // 2体の雷オオカミを召喚
            for (int i = 0; i < 2; i++) {
                ThunderWolfEntity wolf = ModEntities.THUNDER_WOLF.get().create(serverLevel);
                if (wolf != null) {
                    BlockPos spawnPos = player.blockPosition().offset(level.random.nextInt(3) - 1, 0, level.random.nextInt(3) - 1);
                    wolf.moveTo(spawnPos, 0.0F, 0.0F);
                    // プレイヤーになつかせる
                    wolf.tame(player);
                    // 座らせないでおく
                    wolf.setOrderedToSit(false);
                    serverLevel.addFreshEntity(wolf);
                }
            }
            
            // 10分間（12000 ticks）のクールダウンを設定
            player.getCooldowns().addCooldown(this, 12000);
            
            player.displayClientMessage(Component.literal("來の方の獣よ、有れ"), true);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.literal("§7右クリックで雷オオカミを2体召喚する。"));
        tooltipComponents.add(Component.literal("§7召喚されたオオカミは5分間共に戦う。"));
        tooltipComponents.add(Component.literal("§cクールダウン: 10分"));
    }
}