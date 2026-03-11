package com.delphy.legendmagic.network;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.magic.SpellRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CastMagicPacket {
    private final ResourceLocation spellId;
    private final boolean isStart; // 詠唱開始か発動かを判別

    // 1. 送信時に使うコンストラクタ
    public CastMagicPacket(ResourceLocation spellId, boolean isStart) {
        this.spellId = spellId;
        this.isStart = isStart;
    }

    // 2. ネットワーク経由でデータを書き込む（エンコード）
    public static void encode(CastMagicPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.spellId);
        buf.writeBoolean(msg.isStart); // booleanを書き込む
    }

    // 3. ネットワーク経由でデータを受け取る（デコード）
    public static CastMagicPacket decode(FriendlyByteBuf buf) {
        // 書き込んだ順番と同じ順番で読み込む
        return new CastMagicPacket(buf.readResourceLocation(), buf.readBoolean());
    }

    // 4. サーバー側での実際の処理
    public static void handle(CastMagicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // レジストリから魔法を取得
            AbstractMagic spell = SpellRegistry.REGISTRY.get().getValue(msg.spellId);
            if (spell != null) {
                if (msg.isStart) {
                    // 詠唱開始時：共通メソッドでチャットに文言を表示
                    spell.announceChant(player);
                } else {
                    // 詠唱完了時：魔法の実行
                    spell.execute(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}