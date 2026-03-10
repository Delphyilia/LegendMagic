package com.delphy.legendmagic.client.gui;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MagicBookScreen extends Screen {
    private int selectedSlot = 0;

    public MagicBookScreen() {
        super(Component.literal("魔導書"));
    }

    @Override
    protected void init() {
        int x = this.width / 2;

        // --- 1. スロット選択ボタン (1~5番) ---
        for (int s = 0; s < 5; s++) {
            int slotNum = s;
            this.addRenderableWidget(Button.builder(Component.literal("Slot " + (s + 1)), (btn) -> {
                this.selectedSlot = slotNum;
            }).bounds(x - 150, 50 + (s * 25), 50, 20).build());
        }

        // --- 2. 魔法レジストリから全魔法を取得して「セット」ボタンを作成 ---
        // REGISTRY.get().getValues() で、登録済みの全魔法をリストで取得できます
        List<AbstractMagic> allMagics = new ArrayList<>(SpellRegistry.REGISTRY.get().getValues());

        for (int i = 0; i < allMagics.size(); i++) {
            AbstractMagic spell = allMagics.get(i);
            this.addRenderableWidget(Button.builder(Component.literal("セット"), (btn) -> {
                // 現在選択中のスロット(selectedSlot)に魔法インスタンスをセット
                SpellManager.setSpellAt(this.selectedSlot, spell);
            }).bounds(x + 80, 50 + (i * 30), 40, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        int x = this.width / 2;

        graphics.drawCenteredString(this.font, "--- 魔導書: 魔法の編集 ---", x, 15, 0xFFD700);
        graphics.drawString(this.font, "選択中: スロット " + (selectedSlot + 1), x - 150, 35, 0x00FF00);

        // レジストリから魔法リストを取得して描画
        List<AbstractMagic> allMagics = new ArrayList<>(SpellRegistry.REGISTRY.get().getValues());
        for (int i = 0; i < allMagics.size(); i++) {
            AbstractMagic spell = allMagics.get(i);
            graphics.drawString(this.font, spell.getName(), x - 60, 50 + (i * 30), 0xFFFFFF);
            graphics.drawString(this.font, "詠唱: " + spell.getChant(), x - 60, 62 + (i * 30), 0xAAAAAA);
        }

        // --- 3. 現在の装備内容を表示 ---
        graphics.drawCenteredString(this.font, "【 現在の装備 】", x, 180, 0xFFFFFF);
        for (int s = 0; s < 5; s++) {
            AbstractMagic equipped = SpellManager.getEquippedSpellAt(s);
            String name = (equipped != null) ? equipped.getName() : "---";
            graphics.drawCenteredString(this.font, (s + 1) + ":" + name, x - 100 + (s * 50), 195, 0xBBBBBB);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}