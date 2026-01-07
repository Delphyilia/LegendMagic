package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.magic.LightningSpell;
import com.delphy.legendmagic.network.ModNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LegendMagic.MODID, value = Dist.CLIENT)

public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (ModKeyBindings.CAST_LIGHTNING.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // 詠唱文表示
            mc.player.sendSystemMessage(
                    Component.literal(LightningSpell.CHANT)
            );

            // サーバーへ通知
            ModNetwork.sendLightningCast();
        }
    }
}
