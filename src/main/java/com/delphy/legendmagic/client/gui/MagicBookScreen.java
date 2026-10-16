package com.delphy.legendmagic.client.gui;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 刷新された魔導書（Grimoire）GUI。
 * - 左側：10個のスロット一覧（現在刻印されている魔法の表示＆選択）
 * - 右側上：習得済み魔法一覧ボタン（クリックで詳細プレビュー）
 * - 右側下：魔法の詳細情報（国家、詠唱文、消費マナ、詠唱時間）と「スロットにセット」「解除」ボタン
 */
public class MagicBookScreen extends Screen {

    private int selectedSlot = 0;
    private Spell previewSpell = null;

    public MagicBookScreen() {
        super(Component.literal("魔導書"));
    }

    @Override
    protected void init() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        this.clearWidgets();
        int leftX = this.width / 2 - 190;
        int topY = 40;

        // 1. スロット1〜10ボタン（左列: 1〜5、右列: 6〜10）
        for (int s = 0; s < SpellManager.MAX_SPELL_SLOTS; s++) {
            int slotIdx = s;
            int col = s / 5; // 0 or 1
            int row = s % 5;
            int bx = leftX + (col * 85);
            int by = topY + (row * 26);

            this.addRenderableWidget(Button.builder(Component.literal("Slot " + (s + 1)), (btn) -> {
                this.selectedSlot = slotIdx;
            }).bounds(bx, by, 80, 22).build());
        }

        // 2. 習得済み魔法ボタン（中央列、ページネーション対応）
        List<Spell> learnedSpells = getLearnedSpells(player);
        int midX = this.width / 2 - 10;
        int pageSize = 6;
        int totalPages = Math.max(1, (int) Math.ceil((double) learnedSpells.size() / pageSize));
        if (spellListPage >= totalPages) spellListPage = totalPages - 1;

        int startIndex = spellListPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, learnedSpells.size());

        for (int i = startIndex; i < endIndex; i++) {
            Spell spell = learnedSpells.get(i);
            int row = i - startIndex;
            int by = topY + (row * 24);

            String displayName = spell.getName();
            if (displayName.length() > 8) {
                displayName = displayName.substring(0, 7) + "…";
            }

            this.addRenderableWidget(Button.builder(Component.literal(displayName), (btn) -> {
                this.previewSpell = spell;
            }).bounds(midX, by, 95, 20).build());
        }

        // ページ送りボタン
        if (totalPages > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), (btn) -> {
                if (this.spellListPage > 0) {
                    this.spellListPage--;
                    this.rebuildWidgets();
                }
            }).bounds(midX, topY + (pageSize * 24) + 2, 25, 18).build());

            this.addRenderableWidget(Button.builder(Component.literal(">"), (btn) -> {
                if (this.spellListPage < totalPages - 1) {
                    this.spellListPage++;
                    this.rebuildWidgets();
                }
            }).bounds(midX + 70, topY + (pageSize * 24) + 2, 25, 18).build());
        }

        // 3. セットボタン＆解除ボタン（右側下部）
        int actionX = this.width / 2 + 95;
        this.addRenderableWidget(Button.builder(Component.literal("§aスロットにセット"), (btn) -> {
            if (this.previewSpell != null) {
                ModNetwork.sendToServer(new ModNetwork.SetSpellSlotPacket(this.selectedSlot, previewSpell.getSpellId()));
            }
        }).bounds(actionX, 175, 95, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§c刻印解除"), (btn) -> {
            ModNetwork.sendToServer(new ModNetwork.SetSpellSlotPacket(this.selectedSlot, new ResourceLocation("legendmagic", "empty")));
        }).bounds(actionX, 200, 95, 20).build());

        if (this.previewSpell == null && !learnedSpells.isEmpty()) {
            this.previewSpell = learnedSpells.get(0);
        }
    }

    private int spellListPage = 0;

    private List<Spell> getLearnedSpells(Player player) {
        List<Spell> allSpells = new ArrayList<>(SpellRegistry.REGISTRY.get().getValues());
        List<Spell> learned = new ArrayList<>();
        for (Spell spell : allSpells) {
            if (SpellManager.hasLearned(player, spell)) {
                learned.add(spell);
            }
        }
        return learned;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int cx = this.width / 2;

        // タイトル
        graphics.drawCenteredString(this.font, "--- 魔導書: 刻印と解析記録 ---", cx, 15, 0xFFD700);

        // 左エリア：スロット状態
        int leftX = cx - 190;
        graphics.drawString(this.font, "【円環スロット選択】§e(現在: Slot " + (selectedSlot + 1) + ")", leftX, 28, 0xAAAAAA);

        List<Spell> equipped = SpellManager.getEquippedSpells(player);
        for (int s = 0; s < SpellManager.MAX_SPELL_SLOTS; s++) {
            int col = s / 5;
            int row = s % 5;
            int bx = leftX + (col * 85);
            int by = 40 + (row * 26);

            // 選択中スロットのハイライト枠
            if (s == selectedSlot) {
                graphics.renderOutline(bx - 1, by - 1, 82, 24, 0xFFFFD700);
            }

            // 現在セットされている魔法名
            String spellName = "空き";
            int color = 0x777777;
            if (s < equipped.size()) {
                Spell sp = equipped.get(s);
                if (sp != null) {
                    spellName = sp.getName();
                    if (spellName.length() > 5) spellName = spellName.substring(0, 4) + "..";
                    color = sp.getMagicSystem().getPrimaryColor();
                }
            }
            graphics.drawString(this.font, spellName, bx + 42, by + 6, color, false);
        }

        // 中央エリア：習得済み魔法
        int midX = cx - 10;
        graphics.drawString(this.font, "【解析済み魔法】", midX, 28, 0xAAAAAA);
        List<Spell> learnedSpells = getLearnedSpells(player);
        if (learnedSpells.isEmpty()) {
            graphics.drawString(this.font, "習得なし", midX, 45, 0x666666);
        }

        // 右エリア：プレビュー詳細
        int rightX = cx + 90;
        graphics.drawString(this.font, "【魔法の術式情報】", rightX, 28, 0xAAAAAA);

        graphics.fill(rightX - 5, 40, rightX + 105, 170, 0x88000000);
        graphics.renderOutline(rightX - 5, 40, 110, 130, 0xFF445566);

        if (previewSpell != null) {
            graphics.drawString(this.font, previewSpell.getName(), rightX, 46, previewSpell.getMagicSystem().getPrimaryColor());
            graphics.drawString(this.font, "体系: " + previewSpell.getMagicSystem().getDisplayName(), rightX, 60, 0xCCCCCC);
            graphics.drawString(this.font, "消費MP: " + previewSpell.getManaCost(), rightX, 74, 0x66CCFF);
            String castTimeStr = (previewSpell.getCastTime() == 0) ? "即時(無詠唱)" : (previewSpell.getCastTime() / 20.0) + "秒";
            graphics.drawString(this.font, "詠唱: " + castTimeStr, rightX, 88, 0xFFAAAA);

            graphics.drawString(this.font, "§7詠唱文:", rightX, 106, 0x888888);
            String chant = previewSpell.getChant();
            if (chant.length() > 10) {
                graphics.drawString(this.font, chant.substring(0, 10), rightX, 118, 0xEEDD82);
                graphics.drawString(this.font, chant.substring(10), rightX, 128, 0xEEDD82);
            } else {
                graphics.drawString(this.font, chant, rightX, 118, 0xEEDD82);
            }
        } else {
            graphics.drawString(this.font, "魔法を選択して", rightX, 55, 0x888888);
            graphics.drawString(this.font, "詳細を確認", rightX, 68, 0x888888);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}