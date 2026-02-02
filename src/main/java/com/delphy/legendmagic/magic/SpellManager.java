package com.delphy.legendmagic.magic;

public class SpellManager {

    private static MagicType current = MagicType.LIGHTNING;

    public static MagicType getCurrent() {
        return current;
    }

    public static MagicType next() {

        MagicType[] values = MagicType.values();

        int nextIndex = (current.ordinal() + 1) % values.length;

        current = values[nextIndex];

        return current;
    }
}
