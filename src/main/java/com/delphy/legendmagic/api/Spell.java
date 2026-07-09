package com.delphy.legendmagic.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 全ての魔法が実装すべきインターフェース。
 * 魔法のID・名前・詠唱文・説明文・実行ロジックを定義する。
 */
public interface Spell {

    // 魔法を識別する固有ID
    ResourceLocation getSpellId();

    // 魔法の名前
    String getName();

    // 魔法の詠唱文
    String getChant();

    // 魔法の説明文
    String getDescription();

    // 魔法の詠唱時間
    // デフォルトで2秒 (20tick = 1s)
    default int getCastTime() { return 40; }

    // 魔法の実行(本体)
    void execute(LivingEntity caster);

    /**
     * 詠唱中に毎tick呼ばれるクライアント側エフェクト。
     * 魔法ごとに固有の演出をオーバーライドで定義できる。
     * @param player 詠唱中のプレイヤー
     */
    default void onCastingTick(Player player) {
        // デフォルトでは何もしない
    }

    /**
     * 詠唱文を周囲に表示する共通処理
     * @param caster 実行者
     */
    default void announceChant(LivingEntity caster) {
        if (caster.level().isClientSide) return;

        // 名前を付けて、誰が何を唱えているか分かりやすくする
        String name = caster.getName().getString();
        Component text = Component.literal(name + "「" + this.getChant() + "」")
                         .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);

        caster.level().getEntitiesOfClass(ServerPlayer.class, caster.getBoundingBox().inflate(16))
                .forEach(player -> {
                    // 第2引数を true にしてアクションバーに表示
                    player.displayClientMessage(text, true);
                });
    }
}
