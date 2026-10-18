package com.delphy.legendmagic.client;

/**
 * クライアント側で保持する殲滅眼（イーノ・ドゥーエ）の吸収ダメージ蓄積プールデータ。
 */
public class ClientIinoDoueData {
    private static float pool = 0.0F;

    public static void setPool(float value) {
        pool = Math.max(0.0F, value);
    }

    public static float getPool() {
        return pool;
    }
}
