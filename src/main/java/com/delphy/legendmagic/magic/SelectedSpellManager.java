package com.delphy.legendmagic.magic;

public class SelectedSpellManager {

    private static int selectedIndex = 0;

    private static final String[] SPELLS = {
            "lightning",
            "strengthening"
    };

    public static void scrollSpell(int direction) {

        selectedIndex += direction;

        if (selectedIndex < 0) {
            selectedIndex = SPELLS.length - 1;
        }

        if (selectedIndex >= SPELLS.length) {
            selectedIndex = 0;
        }
    }

    public static String getSelectedSpell() {
        return SPELLS[selectedIndex];
    }
}
