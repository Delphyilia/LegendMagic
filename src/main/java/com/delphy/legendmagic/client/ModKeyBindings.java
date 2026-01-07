package com.delphy.legendmagic.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.delphy.legendmagic.LegendMagic;
import com.mojang.blaze3d.platform.InputConstants;

@Mod.EventBusSubscriber(
        modid = LegendMagic.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModKeyBindings {

    public static KeyMapping CAST_LIGHTNING;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        CAST_LIGHTNING = new KeyMapping(
                "key.legendmagic.cast_lightning", // lang key
                InputConstants.KEY_R,              // Rキー
                "key.categories.legendmagic"
        );

        event.register(CAST_LIGHTNING);
    }
}
