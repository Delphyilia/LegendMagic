package com.delphy.legendmagic.item;

import com.delphy.legendmagic.client.gui.MagicBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GrimoireItem extends Item {
    public GrimoireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            // クライアント側（自分のPC）だけでGUIを開く
            openGui();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    // クライアント側専用の処理として分ける（DistExecutorなどを使うのが理想ですが、まずはこれで）
    private void openGui() {
        Minecraft.getInstance().setScreen(new MagicBookScreen());
    }
}