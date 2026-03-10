package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;

public class CastingManager {
    private static AbstractMagic activeSpell = null;
    private static int castTick = 0;
    private static int maxCastTime = 0;

    public static void startCasting(AbstractMagic spell) {
        // すでに同じ魔法を唱えていたらキャンセル（トグル動作）
        if (activeSpell == spell) {
            cancel();
            return;
        }
        activeSpell = spell;
        castTick = 0;
        maxCastTime = spell.getCastTime();
    }

    public static void tick() {
        if (activeSpell == null) return;

        castTick++;

        // 詠唱完了！
        if (castTick >= maxCastTime) {
            ModNetwork.sendCastMagic(activeSpell.getSpellId());
            activeSpell = null; // 終了
        }
    }

    public static void cancel() {
        activeSpell = null;
        castTick = 0;
    }

    public static boolean isCasting() { return activeSpell != null; }
    public static AbstractMagic getActiveSpell() { return activeSpell; }
    public static float getProgress() { return (float)castTick / maxCastTime; }
    public static int getRemainingTicks() { return maxCastTime - castTick; }
}