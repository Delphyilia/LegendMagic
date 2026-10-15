package com.delphy.legendmagic.client.gui;

import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.client.input.ModKeyBindings;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Rキー長押しで展開される8スロット円環ラジアルメニュー。
 * 国ごとのカラーコード枠（ローランド:赤、エスタブール:青、ルーナ:白緑）で描画される。
 */
public class RadialMenuScreen extends Screen {

    private int hoveredSlot = -1;
    private final int slotCount = SpellManager.MAX_SPELL_SLOTS; // 8

    public RadialMenuScreen() {
        super(Component.literal("ラジアルメニュー"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // Rキーを離したら選択を確定して閉じる
        if (!ModKeyBindings.RADIAL_MENU_KEY.isDown()) {
            confirmSelection();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot >= 0) {
            confirmSelection();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void confirmSelection() {
        Player player = Minecraft.getInstance().player;
        if (player != null && hoveredSlot >= 0 && hoveredSlot < slotCount) {
            player.getPersistentData().putInt(SpellManager.TAG_SELECTED_INDEX, hoveredSlot);
            ModNetwork.sendToServer(new ModNetwork.SelectSpellIndexPacket(hoveredSlot));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int cx = this.width / 2;
        int cy = this.height / 2;

        // 1. 半透明の暗幕背景
        graphics.fillGradient(0, 0, this.width, this.height, 0x55000000, 0x88000000);

        // 2. マウスの角度からホバー中スロットを計算
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist >= 20.0D) {
            // 上(12時)を0度とする
            double rad = Math.atan2(dy, dx) + (Math.PI / 2.0D);
            if (rad < 0) rad += Math.PI * 2.0D;
            // 8等分のスロット判定
            hoveredSlot = ((int) Math.round(rad / (Math.PI * 2.0D / slotCount))) % slotCount;
        } else {
            hoveredSlot = SpellManager.getSelectedIndex(player);
        }

        List<Spell> equipped = SpellManager.getEquippedSpells(player);
        int currentSelected = SpellManager.getSelectedIndex(player);

        // 3. 8つのスロットを円環状に描画
        double radius = 80.0D;
        int boxW = 54;
        int boxH = 24;

        for (int s = 0; s < slotCount; s++) {
            double angle = (s * (Math.PI * 2.0D / slotCount)) - (Math.PI / 2.0D);
            int sx = (int) (cx + Math.cos(angle) * radius);
            int sy = (int) (cy + Math.sin(angle) * radius);

            Spell spell = (s < equipped.size()) ? equipped.get(s) : null;
            MagicSystem system = (spell != null) ? spell.getMagicSystem() : null;

            int borderColor = 0xFF555555;
            int bgColor = 0xBB181818;

            if (system != null) {
                borderColor = system.getPrimaryColor();
                bgColor = 0xDD000000 | (system.getDarkColor() & 0x00FFFFFF);
            }

            boolean isHovered = (s == hoveredSlot);
            boolean isSelected = (s == currentSelected);

            if (isHovered) {
                borderColor = 0xFFFFFFFF; // ホバー時は白く発光
            }

            int bx = sx - (boxW / 2);
            int by = sy - (boxH / 2);

            // 外枠と背景
            graphics.fill(bx - 1, by - 1, bx + boxW + 1, by + boxH + 1, borderColor);
            graphics.fill(bx, by, bx + boxW, by + boxH, bgColor);

            // 選択中インジケータ（金枠）
            if (isSelected) {
                graphics.renderOutline(bx - 2, by - 2, boxW + 4, boxH + 4, 0xFFFFD700);
            }

            // スロット番号
            String slotLabel = String.valueOf(s + 1);
            graphics.drawString(this.font, slotLabel, bx + 3, by + 3, 0xAAAAAA, false);

            // 魔法名（短縮表示）
            String spellName = (spell != null) ? spell.getName() : "---";
            if (spellName.length() > 5) {
                spellName = spellName.substring(0, 4) + "..";
            }
            int textColor = (system != null) ? system.getPrimaryColor() : 0x888888;
            graphics.drawCenteredString(this.font, spellName, sx, by + 13, textColor);
        }

        // 4. 中央インフォメーション円環表示
        graphics.fill(cx - 36, cy - 36, cx + 36, cy + 36, 0xCC0A0A0A);
        graphics.renderOutline(cx - 36, cy - 36, 72, 72, 0xFF445566);

        if (hoveredSlot >= 0 && hoveredSlot < slotCount) {
            Spell hoverSpell = (hoveredSlot < equipped.size()) ? equipped.get(hoveredSlot) : null;
            graphics.drawCenteredString(this.font, "Slot " + (hoveredSlot + 1), cx, cy - 26, 0xAAAAAA);

            if (hoverSpell != null) {
                graphics.drawCenteredString(this.font, hoverSpell.getName(), cx, cy - 12, hoverSpell.getMagicSystem().getPrimaryColor());
                graphics.drawCenteredString(this.font, "MP: " + hoverSpell.getManaCost(), cx, cy + 2, 0x66CCFF);
                graphics.drawCenteredString(this.font, hoverSpell.getMagicSystem().getDisplayName(), cx, cy + 15, 0xDDDDDD);
            } else {
                graphics.drawCenteredString(this.font, "未セット", cx, cy - 5, 0x666666);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}
