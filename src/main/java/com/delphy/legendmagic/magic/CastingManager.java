package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;

/**
 * クライアント側の詠唱状態を管理するクラス。
 * 詠唱の開始・進行・完了・キャンセルを制御する。
 */
public class CastingManager {
    private static Spell castingSpell = null;
    private static int elapsedTicks = 0;
    private static int requiredTicks = 0;

    /**
     * 魔法の詠唱を開始する。同じ魔法を再度押した場合はキャンセル。
     */
    public static void startCasting(Spell spell) {
        if (spell == null) return;

        // すでに同じ魔法を唱えていたらキャンセル
        if (castingSpell == spell) {
            cancel();
            return;
        }

        castingSpell = spell;
        elapsedTicks = 0;
        requiredTicks = spell.getCastTime();

        // サーバーへ「詠唱開始」を通知
        ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSpellPacket(spell.getSpellId(), true));
    }

    /**
     * 毎tickの更新処理。詠唱中エフェクトの実行と詠唱完了判定を行う。
     */
    public static void tick() {
        if (castingSpell == null) return;

        elapsedTicks++;

        // 各魔法固有の詠唱中エフェクトを実行
        if (Minecraft.getInstance().player != null) {
            castingSpell.onCastingTick(Minecraft.getInstance().player);
        }

        // 詠唱完了
        if (elapsedTicks >= requiredTicks) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSpellPacket(castingSpell.getSpellId(), false));
            castingSpell = null;
        }
    }

    public static void cancel() {
        castingSpell = null;
        elapsedTicks = 0;
    }

    public static boolean isCasting() { return castingSpell != null; }
    public static Spell getCastingSpell() { return castingSpell; }
    public static float getProgress() { return (float) elapsedTicks / requiredTicks; }
    public static int getRemainingTicks() { return requiredTicks - elapsedTicks; }
}