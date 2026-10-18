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

        boolean hasIinoDoue = com.delphy.legendmagic.util.EyeUtil.hasIinoDoue(mc.player);

        if (hasIinoDoue) {
            // ===== 殲滅眼（イーノ・ドゥーエ）蓄積魔力バー表示 =====
            float pool = com.delphy.legendmagic.client.ClientIinoDoueData.getPool();
            // 蓄積10ダメージあたり5秒換算 (最大60秒)
            int durationSec = Math.min(60, (int) (pool * 0.5F));
            float maxCapacity = 120.0F; // 120ダメージで最大覚醒ゲージ満タン
            float ratio = Math.min(1.0F, pool / maxCapacity);
            int filledWidth = (int) (barWidth * ratio);

            // 1. 漆黒と血痕の外枠
            graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF4A050A);

            // 2. 背景スロット（深淵の暗黒）
            graphics.fill(x, y, x + barWidth, y + barHeight, 0xDD150205);

            // 3. 深紅の魔力蓄積バー
            if (filledWidth > 0) {
                graphics.fill(x, y, x + filledWidth, y + barHeight, 0xFF990022);
                graphics.fill(x, y, x + filledWidth, y + 2, 0xFFFF2255);
            }

            // 4. 蓄積テキストと覚醒可能秒数
            String text = pool > 0.0F
                    ? String.format("§c殲滅蓄積: %.1f §6(覚醒 %ds)", pool, durationSec)
                    : "§4【殲滅眼】魔法吸収待機";
            graphics.drawString(mc.font, text, x, y - 9, 0xFF5555, true);

        } else {
            // ===== 通常マナバー表示 =====
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
}
