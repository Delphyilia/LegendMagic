package com.delphy.legendmagic.magic;

public enum MagicType {

    LIGHTNING("ライトニング"),
    SELF_STRENGTHENING("自己強化");

    private final String displayName;

    MagicType(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
