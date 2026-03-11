package com.delphy.legendmagic.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface AbstractMagic {
    ResourceLocation getSpellId(); // 魔法を識別する固有ID
    String getName();
    String getChant();
    String getDescription();
    default int getCastTime() { return 40; } // デフォルトで2秒 (20tick = 1s)
    void execute(LivingEntity player);


    /**
     * 詠唱文を周囲に表示する共通処理
     * @param caster 実行者
     */
    default void announceChant(LivingEntity caster) {
        if (caster.level().isClientSide) return;

        // 詠唱文を構築 (例: 「我・契約文を…」)
        Component text = Component.literal("「" + this.getChant() + "」")
                .withStyle(caster instanceof Player ? ChatFormatting.GOLD : ChatFormatting.RED)
                .withStyle(ChatFormatting.ITALIC);

        // 周囲16ブロック以内のプレイヤーにのみメッセージを表示（ログの氾濫を防ぐため）
        caster.level().getEntitiesOfClass(ServerPlayer.class, caster.getBoundingBox().inflate(16))
                .forEach(player -> player.sendSystemMessage(text));
    }
}