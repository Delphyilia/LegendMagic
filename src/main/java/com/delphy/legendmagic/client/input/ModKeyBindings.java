package com.delphy.legendmagic.client.input;

import org.lwjgl.glfw.GLFW;

import com.delphy.legendmagic.LegendMagic;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = LegendMagic.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModKeyBindings {

    // 魔法発動キー
    public static final KeyMapping CAST_MAGIC_KEY =
            new KeyMapping(
                    "key.legendmagic.cast",
                    GLFW.GLFW_KEY_Z,
                    "key.categories.legendmagic"
            );

    // 魔法切り替えキー
    public static final KeyMapping MAGIC_SWITCH_KEY =
            new KeyMapping(
                    "key.legendmagic.switch",
                    GLFW.GLFW_KEY_M,
                    "key.categories.legendmagic"
            );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CAST_MAGIC_KEY);
        event.register(MAGIC_SWITCH_KEY);
    }
}
