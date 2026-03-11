package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.network.CastMagicPacket;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;

public class CastingManager {
    private static AbstractMagic activeSpell = null;
    private static int castTick = 0;
    private static int maxCastTime = 0;

    public static void startCasting(AbstractMagic spell) {
        if (spell == null) return;

        // すでに同じ魔法を唱えていたらキャンセル
        if (activeSpell == spell) {
            cancel();
            return;
        }

        activeSpell = spell;
        castTick = 0;
        maxCastTime = spell.getCastTime();

        // ⭐ サーバーへ「詠唱開始」を通知（第2引数をtrueに）
        // これによりサーバー側で announceChant が実行されます
        ModNetwork.CHANNEL.sendToServer(new CastMagicPacket(spell.getSpellId(), true));
    }

    public static void tick() {
        if (activeSpell == null) return;

        castTick++;

        // 詠唱完了！
        if (castTick >= maxCastTime) {
            // ⭐ サーバーへ「魔法実行」を通知（第2引数をfalseに）
            ModNetwork.CHANNEL.sendToServer(new CastMagicPacket(activeSpell.getSpellId(), false));
            activeSpell = null;
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