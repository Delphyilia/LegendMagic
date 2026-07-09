package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.network.ModNetwork;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 魔法データの同期およびプレイヤーライフサイクルイベントのハンドラー。
 * - 魔法キャスト時の学習処理（複写眼による解析）
 * - ログイン / リスポーン / ディメンション移動時のデータ同期
 */
@Mod.EventBusSubscriber
public class SpellDataSyncHandler {

    /**
     * 敵が魔法を使った時、複写眼を持つプレイヤーが視認していれば学習する
     */
    @SubscribeEvent
    public static void onMagicCast(MagicCastEvent event) {
        if (event.getCaster() instanceof ServerPlayer) return; // プレイヤー自身の魔法は無視

        // ワールド内の近くのプレイヤーをチェック
        for (ServerPlayer player : event.getCaster().level().getEntitiesOfClass(ServerPlayer.class, event.getCaster().getBoundingBox().inflate(32))) {

            // 1. アルファスティグマ（複写眼）を装備しているか
            if (!EyeUtil.hasAlphaStigma(player)) continue;

            // 2. 敵が視界に入っているか（内積計算）
            if (isLookingAt(player, event.getCaster())) {
                Spell spell = event.getMagic();

                // 3. すでに習得済みかチェックし、未習得なら魔導書に追加
                if (!SpellManager.hasLearned(player, spell)) {
                    SpellManager.learnSpell(player, spell);
                    player.sendSystemMessage(Component.literal("§c[複写眼] §f魔法を解析しました: " + spell.getName()));
                }
            }
        }
    }

    /**
     * プレイヤーが対象を「見ている」かを判定する（視野角 約60度）
     */
    private static boolean isLookingAt(ServerPlayer player, net.minecraft.world.entity.Entity target) {
        Vec3 lookVec = player.getLookAngle().normalize();
        Vec3 relVec = target.position().subtract(player.position()).normalize();
        double dot = lookVec.dot(relVec);
        return dot > 0.85; // 0.85 = 約60度の視野
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncSpellDataToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncSpellDataToClient(player);
        }
    }

    /**
     * サーバーのNBTデータをクライアントに同期する
     */
    private static void syncSpellDataToClient(ServerPlayer player) {
        CompoundTag playerNbt = player.getPersistentData();
        CompoundTag syncData = new CompoundTag();

        // 習得済みリストのコピー
        if (playerNbt.contains(SpellManager.TAG_LEARNED)) {
            syncData.put(SpellManager.TAG_LEARNED, playerNbt.get(SpellManager.TAG_LEARNED));
        }

        // 装備スロットのコピー
        if (playerNbt.contains(SpellManager.TAG_EQUIPPED)) {
            syncData.put(SpellManager.TAG_EQUIPPED, playerNbt.get(SpellManager.TAG_EQUIPPED));
        }

        if (!syncData.isEmpty()) {
            ModNetwork.sendToClient(new ModNetwork.SyncSpellDataPacket(syncData), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 死亡時などのプレイヤーの入れ替え時にデータを引き継ぐ
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();

        String[] tagsToCopy = {
                SpellManager.TAG_LEARNED,
                SpellManager.TAG_EQUIPPED,
                SpellManager.TAG_SELECTED_INDEX
        };

        for (String tag : tagsToCopy) {
            if (oldData.contains(tag)) {
                newData.put(tag, oldData.get(tag));
            }
        }

        // リスポーン直後はクライアント側が空になるので同期を送る
        if (event.getEntity() instanceof ServerPlayer newServerPlayer) {
            syncSpellDataToClient(newServerPlayer);
        }
    }
}
