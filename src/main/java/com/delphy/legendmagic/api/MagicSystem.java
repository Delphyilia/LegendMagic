package com.delphy.legendmagic.api;

public enum MagicSystem {
    ROLAND("ローランド", 0xFFFF3333, 0xFF881111),     // 赤系 (攻撃)
    ESTABUL("エスタブール", 0xFF3388FF, 0xFF113388), // 青系 (契約・使役・空間)
    RUNA("ルーナ", 0xFF44EE88, 0xFF116633);         // 白緑系 (生活・便利)

    private final String displayName;
    private final int primaryColor;
    private final int darkColor;

    MagicSystem(String displayName, int primaryColor, int darkColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getDarkColor() {
        return darkColor;
    }
}
