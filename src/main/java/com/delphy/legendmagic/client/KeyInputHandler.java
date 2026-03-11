package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.magic.CastingManager;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.util.EyeUtil;
import com.delphy.legendmagic.client.input.ModKeyBindings;

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
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ===== 魔法発動キー =====
        // KeyInputHandler.java 内の魔法発動キー処理
        if (ModKeyBindings.CAST_MAGIC_KEY.consumeClick()) {
            if (!EyeUtil.hasAlphaStigma(mc.player)) return;

            AbstractMagic currentSpell = SpellManager.getCurrent(mc.player);
            if (currentSpell != null) {
                // 即発動ではなく、詠唱を開始する
                CastingManager.startCasting(currentSpell);
            }
        }

// Tickの最後でCastingManagerを更新
        CastingManager.tick();

        // ===== 魔法切替キー =====
        if (ModKeyBindings.MAGIC_SWITCH_KEY.consumeClick()) {
            SpellManager.next(mc.player);

            AbstractMagic nextSpell = SpellManager.getCurrent(mc.player);
            if (nextSpell != null) {
                mc.player.displayClientMessage(
                        Component.literal("選択中: " + nextSpell.getName()),
                        true
                );
            }
        }
    }
}