package com.delphy.legendmagic.client;

/**
 * クライアント側でHUD描画用に保持するマナデータキャッシュ。
 */
public class ClientManaData {
    private static float currentMana = 100.0F;
    private static float maxMana = 100.0F;

    public static void set(float mana, float max) {
        currentMana = mana;
        maxMana = max;
    }

    public static float getMana() {
        return currentMana;
    }

    public static float getMaxMana() {
        return maxMana;
    }

    public static float getRatio() {
        return maxMana > 0 ? Math.max(0.0F, Math.min(1.0F, currentMana / maxMana)) : 0.0F;
    }
}
