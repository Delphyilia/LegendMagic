package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.client.input.ModKeyBindings;
import com.delphy.legendmagic.magic.CastingManager;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * クライアント側の毎tick処理を管理するハンドラー。
 * キー入力の処理と詠唱マネージャーの更新を行う。
 */
@Mod.EventBusSubscriber(modid = LegendMagic.MODID, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ===== 魔法発動キー =====
        if (ModKeyBindings.CAST_MAGIC_KEY.consumeClick()) {
            if (!EyeUtil.hasAlphaStigma(mc.player)) return;

            Spell currentSpell = SpellManager.getSelectedSpell(mc.player);
            if (currentSpell != null) {
                CastingManager.startCasting(currentSpell);
            }
        }

        // 毎tickで詠唱マネージャーを更新
        CastingManager.tick();

        // ===== 魔法切替キー =====
        if (ModKeyBindings.MAGIC_SWITCH_KEY.consumeClick()) {
            SpellManager.cycleSelectedSpell(mc.player);

            Spell nextSpell = SpellManager.getSelectedSpell(mc.player);
            if (nextSpell != null) {
                mc.player.displayClientMessage(
                        Component.literal("選択中: " + nextSpell.getName()),
                        true
                );
            }
        }
    }
}
