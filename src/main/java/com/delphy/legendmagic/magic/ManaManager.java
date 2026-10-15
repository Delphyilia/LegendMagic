package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * プレイヤーのマナを管理するマネージャークラス。
 * データはプレイヤーのPersistentDataに保存され、定期的に自動回復およびクライアント同期される。
 */
public class ManaManager {
    public static final String TAG_MANA = "LegendMagic_Mana";
    public static final String TAG_MAX_MANA = "LegendMagic_MaxMana";
    public static final float DEFAULT_MAX_MANA = 100.0F;
    public static final float REGEN_PER_SECOND = 5.0F; // 毎秒5マナ回復 (20秒で全快)

    public static float getMana(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(TAG_MANA)) {
            data.putFloat(TAG_MANA, DEFAULT_MAX_MANA);
        }
        return data.getFloat(TAG_MANA);
    }

    public static float getMaxMana(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(TAG_MAX_MANA)) {
            data.putFloat(TAG_MAX_MANA, DEFAULT_MAX_MANA);
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
        player.getPersistentData().putFloat(TAG_MAX_MANA, Math.max(1.0F, maxMana));
        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    /**
     * マナを消費する。足りていれば消費して true、足りなければ false を返す。
     */
    public static boolean consumeMana(Player player, float cost) {
        float current = getMana(player);
        if (current >= cost) {
            setMana(player, current - cost);
            return true;
        }
        return false;
    }

    /**
     * マナを回復・追加する。
     */
    public static void addMana(Player player, float amount) {
        setMana(player, getMana(player) + amount);
    }

    /**
     * サーバーtickごとの更新処理（毎秒5%の自動回復と定期同期）
     */
    public static void tickServer(ServerPlayer player) {
        float current = getMana(player);
        float max = getMaxMana(player);

        if (current < max) {
            float newMana = Math.min(max, current + (REGEN_PER_SECOND / 20.0F));
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
