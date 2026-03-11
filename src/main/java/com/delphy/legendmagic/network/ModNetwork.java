package com.delphy.legendmagic.network;

import com.delphy.legendmagic.LegendMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(LegendMagic.MODID, "main"), // IDを一貫させる
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // 1. 魔法発動パケット (Client -> Server)
        CHANNEL.registerMessage(
                packetId++,
                CastMagicPacket.class,
                CastMagicPacket::encode,
                CastMagicPacket::decode,
                CastMagicPacket::handle
        );

        // 2. 習得魔法同期パケット (Server -> Client) ⭐追加
        CHANNEL.registerMessage(
                packetId++,
                SyncLearnedSpellsPacket.class,
                SyncLearnedSpellsPacket::encode,
                SyncLearnedSpellsPacket::decode,
                SyncLearnedSpellsPacket::handle
        );
    }

    /**
     * 魔法発動パケットをサーバーへ送信
     */
    public static void sendCastMagic(ResourceLocation spellId, boolean isStart) {
        if (CHANNEL != null) {
            CHANNEL.sendToServer(new CastMagicPacket(spellId, isStart));
        }
    }

    /**
     * 習得情報をクライアントへ送信 ⭐追加
     */
    public static void sendToClient(Object packet, net.minecraft.server.level.ServerPlayer player) {
        if (CHANNEL != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}