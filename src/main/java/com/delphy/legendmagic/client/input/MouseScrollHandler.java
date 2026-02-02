package com.delphy.legendmagic.client.input;

import com.delphy.legendmagic.magic.SelectedSpellManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MouseScrollHandler {

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        // ⭐ Mキーが押されているか
        if (ModKeyBindings.MAGIC_SWITCH_KEY.isDown()) {

            int direction = event.getScrollDelta() > 0 ? 1 : -1;

            SelectedSpellManager.scrollSpell(direction);

            event.setCanceled(true);
        }
    }
}
