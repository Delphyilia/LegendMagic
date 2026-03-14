package com.delphy.legendmagic.client.gui;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import com.delphy.legendmagic.network.C2SSetSpellPacket;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class MagicBookScreen extends Screen {
    private int selectedSlot = 0;

    public MagicBookScreen() {
        super(Component.literal("魔導書"));
    }

    @Override
    protected void init() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        int x = this.width / 2;

        // --- 1. スロット選択ボタン (1~5番) ---
        for (int s = 0; s < 5; s++) {
            int slotNum = s;
            this.addRenderableWidget(Button.builder(Component.literal("Slot " + (s + 1)), (btn) -> {
                this.selectedSlot = slotNum;
            }).bounds(x - 150, 50 + (s * 25), 50, 20).build());
        }

        // --- 2. 習得済み魔法だけを表示して「セット」ボタンを作成 ---
        List<AbstractMagic> learnedMagics = getLearnedMagics(player);

        for (int i = 0; i < learnedMagics.size(); i++) {
            AbstractMagic spell = learnedMagics.get(i);
            // MagicBookScreen.java 内のセットボタン部分を修正
            this.addRenderableWidget(Button.builder(Component.literal("セット"), (btn) -> {
                // 直接 SpellManager を呼ぶのをやめ、サーバーにパケットを送る
                ModNetwork.sendToServer(new C2SSetSpellPacket(this.selectedSlot, spell.getSpellId()));

                // クライアント側でも即座に反映して見せたい場合は、
                // 引き続き SpellManager.setSpellAt を呼んでも良いですが、
                // サーバーからの返信パケット（SyncLearnedSpellsPacket）で同期されるのが理想です。
            }).bounds(x + 80, 50 + (i * 30), 40, 20).build());
        }
    }

    /**
     * レジストリ全体の中から、プレイヤーが習得している魔法だけを抽出する
     */
    private List<AbstractMagic> getLearnedMagics(Player player) {
        List<AbstractMagic> allMagics = new ArrayList<>(SpellRegistry.REGISTRY.get().getValues());
        List<AbstractMagic> learned = new ArrayList<>();
        for (AbstractMagic spell : allMagics) {
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
        int x = this.width / 2;

        graphics.drawCenteredString(this.font, "--- 魔導書: 魔法の解析記録 ---", x, 15, 0xFFD700);
        graphics.drawString(this.font, "セット先: スロット " + (selectedSlot + 1), x - 150, 35, 0x00FF00);

        // 習得済み魔法リストの描画
        List<AbstractMagic> learnedMagics = getLearnedMagics(player);
        if (learnedMagics.isEmpty()) {
            graphics.drawCenteredString(this.font, "解析された魔法はありません。複写眼で敵を観察してください。", x, 100, 0xAAAAAA);
        } else {
            for (int i = 0; i < learnedMagics.size(); i++) {
                AbstractMagic spell = learnedMagics.get(i);
                graphics.drawString(this.font, spell.getName(), x - 60, 50 + (i * 30), 0xFFFFFF);
                graphics.drawString(this.font, "詠唱: " + spell.getChant(), x - 60, 62 + (i * 30), 0xAAAAAA);
            }
        }

        // --- 3. 現在の装備内容を表示 ---
        graphics.drawCenteredString(this.font, "【 刻印済みの魔法 】", x, 180, 0xFFFFFF);
        List<AbstractMagic> equipped = SpellManager.getEquippedSpells(player);
        for (int s = 0; s < 5; s++) {
            // EquippedSpellsのリストから安全に取得
            String name = "---";
            if (s < equipped.size()) {
                AbstractMagic spell = equipped.get(s);
                if (spell != null) name = spell.getName();
            }
            graphics.drawCenteredString(this.font, (s + 1) + ":" + name, x - 100 + (s * 45), 195, 0xBBBBBB);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}