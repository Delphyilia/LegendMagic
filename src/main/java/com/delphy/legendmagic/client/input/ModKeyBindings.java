package com.delphy.legendmagic.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT) // サーバー側でのロードを物理的に拒否する設定
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.legendmagic";

    // static final で即座に new するのをやめる
    public static KeyMapping CAST_MAGIC_KEY;
    public static KeyMapping MAGIC_SWITCH_KEY;

    // クライアント側でのみ呼ばれる初期化・登録メソッド
    public static void init(RegisterKeyMappingsEvent event) {
        CAST_MAGIC_KEY = new KeyMapping(
                "key.legendmagic.cast",
                GLFW.GLFW_KEY_Z,
                CATEGORY
        );
        MAGIC_SWITCH_KEY = new KeyMapping(
                "key.legendmagic.switch",
                GLFW.GLFW_KEY_M,
                CATEGORY
        );

        event.register(CAST_MAGIC_KEY);
        event.register(MAGIC_SWITCH_KEY);
    }
}