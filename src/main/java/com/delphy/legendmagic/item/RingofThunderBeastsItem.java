package com.delphy.legendmagic.item;

import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.entity.ThunderWolfEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RingofThunderBeastsItem extends RelicItem {

    public RingofThunderBeastsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            int relicLevel = getRelicLevel(itemStack);
            int summonCount = getSummonCount(relicLevel);
            int cooldownTicks = getCooldownTicks(relicLevel);

            // 強化段階に応じた数の雷オオカミを召喚
            for (int i = 0; i < summonCount; i++) {
                ThunderWolfEntity wolf = ModEntities.THUNDER_WOLF.get().create(serverLevel);
                if (wolf != null) {
                    BlockPos spawnPos = player.blockPosition().offset(level.random.nextInt(3) - 1, 0, level.random.nextInt(3) - 1);
                    wolf.moveTo(spawnPos, 0.0F, 0.0F);
                    // 召喚者に基づきステータスを動的スケーリング・なつかせ
                    wolf.setSummoner(player);
                    serverLevel.addFreshEntity(wolf);
                }
            }

            // 強化段階に応じたクールダウンを設定
            player.getCooldowns().addCooldown(this, cooldownTicks);

            player.displayClientMessage(Component.literal("來の方の獣よ、有れ"), true);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}