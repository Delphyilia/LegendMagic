package com.delphy.legendmagic.util;

import com.delphy.legendmagic.item.LegendMagicItems;

import net.minecraft.world.entity.player.Player;

import top.theillusivec4.curios.api.CuriosApi;

public class EyeUtil {

    public static boolean hasAlphaStigma(Player player) {

        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, LegendMagicItems.ALPHA_STIGMA.get())
                .isPresent();
    }
}
