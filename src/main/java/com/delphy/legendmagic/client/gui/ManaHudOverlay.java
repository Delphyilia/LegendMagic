package com.delphy.legendmagic.client.gui;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.client.ClientManaData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 画面右下（ホットバー右脇）に表示される青いマナバーHUD。
 */
@Mod.EventBusSubscriber(modid = LegendMagic.MODID, value = Dist.CLIENT)
public class ManaHudOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator() || mc.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int barWidth = 80;
        int barHeight = 8;

        // ホットバー右脇の位置を計算
        int x = (screenWidth / 2) + 98;
        if (x + barWidth > screenWidth - 6) {
            x = screenWidth - barWidth - 6;
        }
        int y = screenHeight - 16;

        float mana = ClientManaData.getMana();
        float maxMana = ClientManaData.getMaxMana();
        float ratio = ClientManaData.getRatio();
        int filledWidth = (int) (barWidth * ratio);

        // 1. 外枠 (ダークシアン)
        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF0A1E32);

        // 2. 背景スロット (半透明ダークブルー)
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xAA001020);

        // 3. マナ残量バー (深青〜水色ハイライト)
        if (filledWidth > 0) {
            graphics.fill(x, y, x + filledWidth, y + barHeight, 0xFF0077EE);
            graphics.fill(x, y, x + filledWidth, y + 2, 0xFF33CCFF);
        }

        // 4. マナ数値テキスト表示
        String text = String.format("MP %d/%d", (int) mana, (int) maxMana);
        graphics.drawString(mc.font, text, x, y - 9, 0x77DDFF, true);
    }
}
