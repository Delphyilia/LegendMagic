package com.delphy.legendmagic.client.gui;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.magic.CastingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LegendMagic.MODID, value = Dist.CLIENT)
public class CastingGui {
    // 魔方陣のテクスチャ（後で作る必要があります）
    private static final ResourceLocation MAGIC_CIRCLE = new ResourceLocation(LegendMagic.MODID, "textures/gui/magic_circle.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!CastingManager.isCasting()) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int x = event.getWindow().getGuiScaledWidth() / 2;
        int y = event.getWindow().getGuiScaledHeight() / 2;

        // 1. 残り秒数の表示
        float seconds = (float)CastingManager.getRemainingTicks() / 20.0f;
        String text = String.format("%.1f s", seconds);
        graphics.drawCenteredString(mc.font, text, x, y + 30, 0x00FFFF);

        // 2. 魔法名の表示
        graphics.drawCenteredString(mc.font, "詠唱中: " + CastingManager.getActiveSpell().getName(), x, y + 45, 0xFFFFFF);

        // 3. 魔方陣の描画 (回転させるとカッコいい)
        // graphics.pose().pushPose();
        // graphics.pose().translate(x, y, 0);
        // graphics.pose().mulPose(Axis.ZP.rotationDegrees(System.currentTimeMillis() / 10 % 360));
        // graphics.blit(MAGIC_CIRCLE, -32, -32, 0, 0, 64, 64, 64, 64);
        // graphics.pose().popPose();
    }
}