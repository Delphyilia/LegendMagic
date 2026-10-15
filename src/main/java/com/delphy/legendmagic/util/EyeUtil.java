package com.delphy.legendmagic.util;

import com.delphy.legendmagic.item.LegendMagicItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public class EyeUtil {

    public static final String TAG_IINO_DOUE_POOL = "LegendMagic_IinoDouePool";

    /**
     * 複写眼（アルファ・スティグマ）を装備しているか
     */
    public static boolean hasAlphaStigma(net.minecraft.world.entity.LivingEntity entity) {
        if (entity == null) return false;
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(entity, LegendMagicItems.ALPHA_STIGMA.get())
                .isPresent();
    }

    /**
     * 殲滅眼（イーノ・ドゥーエ）を装備しているか
     */
    public static boolean hasIinoDoue(net.minecraft.world.entity.LivingEntity entity) {
        if (entity == null) return false;
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(entity, LegendMagicItems.IINO_DOUE.get())
                .isPresent();
    }

    /**
     * 殲滅眼が蓄積している吸収魔法ダメージ量を取得
     */
    public static float getIinoDouePool(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.contains(TAG_IINO_DOUE_POOL) ? tag.getFloat(TAG_IINO_DOUE_POOL) : 0.0F;
    }

    /**
     * 殲滅眼に魔法ダメージを蓄積プール
     */
    public static void addIinoDouePool(Player player, float amount) {
        CompoundTag tag = player.getPersistentData();
        float current = getIinoDouePool(player);
        tag.putFloat(TAG_IINO_DOUE_POOL, current + amount);
    }

    /**
     * 殲滅眼の蓄積プールを全消費して取り出す
     */
    public static float consumeIinoDouePool(Player player) {
        CompoundTag tag = player.getPersistentData();
        float current = getIinoDouePool(player);
        tag.putFloat(TAG_IINO_DOUE_POOL, 0.0F);
        return current;
    }
}
