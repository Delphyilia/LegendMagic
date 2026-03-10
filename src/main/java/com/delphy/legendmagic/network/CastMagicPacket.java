package com.delphy.legendmagic.network;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.magic.SpellRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CastMagicPacket {
    // 魔法を特定するためのID（ResourceLocation）を保持する
    private final ResourceLocation spellId;

    public CastMagicPacket(ResourceLocation spellId) {
        this.spellId = spellId;
    }

    // エンコード: 魔法のIDをパケットに書き込む
    public static void encode(CastMagicPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.spellId);
    }

    // デコード: パケットから魔法のIDを読み取る
    public static CastMagicPacket decode(FriendlyByteBuf buf) {
        return new CastMagicPacket(buf.readResourceLocation());
    }

    // サーバー側での処理
    public static void handle(CastMagicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // デバッグログを追加：サーバーに届いたIDを表示
            System.out.println("サーバー受信魔法ID: " + msg.spellId);

            AbstractMagic spell = SpellRegistry.REGISTRY.get().getValue(msg.spellId);

            if (spell != null) {
                System.out.println("魔法実行開始: " + spell.getName());
                spell.execute(player);
            } else {
                System.out.println("エラー: 魔法が見つかりません ID=" + msg.spellId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}