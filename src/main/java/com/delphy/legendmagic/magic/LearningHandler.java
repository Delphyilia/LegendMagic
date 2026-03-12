package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.network.ModNetwork;
import com.delphy.legendmagic.network.SyncLearnedSpellsPacket;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

import java.util.Objects;

@Mod.EventBusSubscriber
public class LearningHandler {

    @SubscribeEvent
    public static void onMagicCast(MagicCastEvent event) {
        if (event.getCaster() instanceof ServerPlayer) return; // プレイヤー自身の魔法は無視

        // ワールド内の全プレイヤーをチェック（または近くのプレイヤーのみ）
        for (ServerPlayer player : event.getCaster().level().getEntitiesOfClass(ServerPlayer.class, event.getCaster().getBoundingBox().inflate(32))) {

            // 1. アルファスティグマ（複写眼）を装備しているか
            if (!EyeUtil.hasAlphaStigma(player)) continue;

            // 2. 敵が視界に入っているか（内積計算）
            if (isLookingAt(player, event.getCaster())) {
                AbstractMagic magic = event.getMagic();

                // 3. すでに習得済みかチェックし、未習得なら魔導書に追加
                if (!SpellManager.hasLearned(player, magic)) {
                    SpellManager.learnSpell(player, magic);
                    player.sendSystemMessage(Component.literal("§c[複写眼] §f魔法を解析しました: " + magic.getName()));
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
            syncLearnedData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncLearnedData(player);
        }
    }

    /**
     * サーバーのNBTデータをクライアントに同期する共通メソッド
     */
    private static void syncLearnedData(ServerPlayer player) {
        CompoundTag playerNbt = player.getPersistentData();
        CompoundTag syncData = new CompoundTag();

        // 1. 習得済みリストのコピー
        if (playerNbt.contains(SpellManager.TAG_LEARNED)) {
            syncData.put(SpellManager.TAG_LEARNED, playerNbt.get(SpellManager.TAG_LEARNED));
        }

        // 2. 装備スロットのコピー
        if (playerNbt.contains(SpellManager.TAG_EQUIPPED)) {
            syncData.put(SpellManager.TAG_EQUIPPED, playerNbt.get(SpellManager.TAG_EQUIPPED));
        }

        // クライアントへ送信（SyncLearnedSpellsPacketはCompoundTagを丸ごとマージする設計なのでこれでOK）
        if (!syncData.isEmpty()) {
            ModNetwork.sendToClient(new SyncLearnedSpellsPacket(syncData), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 死亡時などのプレイヤーの入れ替え時にデータを引き継ぐ
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();

        // 全てのデータを新しいプレイヤーインスタンスにコピー
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

        // リスポーン直後はクライアント側が空になるので、ここでも同期を送る
        if (event.getEntity() instanceof ServerPlayer newServerPlayer) {
            syncLearnedData(newServerPlayer);
        }
    }
}