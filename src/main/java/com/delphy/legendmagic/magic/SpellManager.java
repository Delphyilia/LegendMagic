package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.AbstractMagic;
import java.util.ArrayList;
import java.util.List;

public class SpellManager {
    // プレイヤーが現在セットしている最大5つの魔法（インターフェースで保持）
    private static final List<AbstractMagic> EQUIPPED_SPELLS = new ArrayList<>();
    private static int selectedSlotIndex = 0;

    // 初期化処理
    public static void init() {
        // 初期状態で何もセットされていない場合、レジストリから取得してセットするなどの処理が可能
        // ここでは空リストのままにし、魔導書GUIからセットさせるのが一般的です。
    }

    /**
     * 現在選択中の魔法を取得
     */
    public static AbstractMagic getCurrent() {
        if (EQUIPPED_SPELLS.isEmpty()) return null;

        // インデックスが範囲外にならないよう調整
        if (selectedSlotIndex >= EQUIPPED_SPELLS.size()) {
            selectedSlotIndex = 0;
        }

        return EQUIPPED_SPELLS.get(selectedSlotIndex);
    }

    /**
     * 次の魔法へ切り替え
     */
    public static void next() {
        if (EQUIPPED_SPELLS.isEmpty()) return;
        selectedSlotIndex = (selectedSlotIndex + 1) % EQUIPPED_SPELLS.size();
    }

    /**
     * 魔法をセットするメソッド（GUIから呼び出し）
     * @param slot 0~4のスロット番号
     * @param spell セットする魔法のインスタンス
     */
    public static void setSpellAt(int slot, AbstractMagic spell) {
        if (slot < 0 || slot >= 5) return;

        // リストのサイズをスロット数に合わせる調整
        while (EQUIPPED_SPELLS.size() <= slot) {
            EQUIPPED_SPELLS.add(null);
        }

        EQUIPPED_SPELLS.set(slot, spell);
    }

    /**
     * 指定したスロットの魔法を取得
     */
    public static AbstractMagic getEquippedSpellAt(int slot) {
        if (slot >= 0 && slot < EQUIPPED_SPELLS.size()) {
            return EQUIPPED_SPELLS.get(slot);
        }
        return null;
    }
}