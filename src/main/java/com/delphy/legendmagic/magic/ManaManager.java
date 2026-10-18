package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.Config;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * プレイヤーのマナを管理するマネージャークラス。
 * データはプレイヤーのPersistentDataに保存され、定期的に自動回復およびクライアント同期される。
 * 魔法を使用するほど消費マナに比例して最大マナが成長する。
 */
public class ManaManager {
    public static final String TAG_MANA = "LegendMagic_Mana";
    public static final String TAG_MAX_MANA = "LegendMagic_MaxMana";
    public static final float FALLBACK_DEFAULT_MAX_MANA = 10000.0F;
    public static final float FALLBACK_REGEN_PER_SECOND = 50.0F;

    public static float getDefaultMaxMana() {
        try {
            return Config.DEFAULT_MAX_MANA.get().floatValue();
        } catch (Exception e) {
            return FALLBACK_DEFAULT_MAX_MANA;
        }
    }

    public static float getMaxManaCap() {
        try {
            return Config.MAX_MANA_CAP.get().floatValue();
        } catch (Exception e) {
            return 50000.0F;
        }
    }

    public static float getRegenPerSecond() {
        try {
            return Config.MANA_REGEN_PER_SECOND.get().floatValue();
        } catch (Exception e) {
            return FALLBACK_REGEN_PER_SECOND;
        }
    }

    public static float getGrowthRate() {
        try {
            return Config.MANA_GROWTH_RATE.get().floatValue();
        } catch (Exception e) {
            return 0.01F;
        }
    }

    public static float getMana(Player player) {
        CompoundTag data = player.getPersistentData();
        float defaultMax = getDefaultMaxMana();
        if (!data.contains(TAG_MANA)) {
            data.putFloat(TAG_MANA, defaultMax);
        }
        return data.getFloat(TAG_MANA);
    }

    public static float getMaxMana(Player player) {
        CompoundTag data = player.getPersistentData();
        float defaultMax = getDefaultMaxMana();
        if (!data.contains(TAG_MAX_MANA) || data.getFloat(TAG_MAX_MANA) < defaultMax) {
            data.putFloat(TAG_MAX_MANA, defaultMax);
            if (!data.contains(TAG_MANA) || data.getFloat(TAG_MANA) < defaultMax) {
                data.putFloat(TAG_MANA, defaultMax);
            }
        }
        return data.getFloat(TAG_MAX_MANA);
    }

    public static void setMana(Player player, float mana) {
        float max = getMaxMana(player);
        float clamped = Math.max(0.0F, Math.min(mana, max));
        player.getPersistentData().putFloat(TAG_MANA, clamped);

        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public static void setMaxMana(Player player, float maxMana) {
        float cap = getMaxManaCap();
        float clamped = Math.min(cap, Math.max(1.0F, maxMana));
        player.getPersistentData().putFloat(TAG_MAX_MANA, clamped);
        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    /**
     * マナを消費し、消費量に応じて最大マナを成長させる。
     */
    public static boolean consumeMana(Player player, float cost) {
        float current = getMana(player);
        if (current >= cost) {
            setMana(player, current - cost);
            growMana(player, cost);
            return true;
        }
        return false;
    }

    /**
     * 魔法使用による最大マナの成長処理（消費マナの一定割合を加算）
     */
    private static void growMana(Player player, float cost) {
        float rate = getGrowthRate();
        if (rate <= 0.0F) return;

        float growth = cost * rate;
        float currentMax = getMaxMana(player);
        float cap = getMaxManaCap();

        if (currentMax < cap) {
            float newMax = Math.min(cap, currentMax + growth);
            setMaxMana(player, newMax);
        }
    }

    /**
     * マナを回復・追加する。
     */
    public static void addMana(Player player, float amount) {
        setMana(player, getMana(player) + amount);
    }

    /**
     * サーバーtickごとの更新処理（毎秒自然回復と定期同期）
     */
    public static void tickServer(ServerPlayer player) {
        float current = getMana(player);
        float max = getMaxMana(player);
        float regen = getRegenPerSecond();

        if (current < max && regen > 0.0F) {
            float newMana = Math.min(max, current + (regen / 20.0F));
            player.getPersistentData().putFloat(TAG_MANA, newMana);
        }

        // 1秒に1回（20tickごと）クライアントへ同期
        if (player.tickCount % 20 == 0) {
            syncToClient(player);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        ModNetwork.sendToClient(new ModNetwork.SyncManaPacket(getMana(player), getMaxMana(player)), player);
    }
}
