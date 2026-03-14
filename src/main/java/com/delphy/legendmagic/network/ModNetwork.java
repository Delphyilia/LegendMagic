package com.delphy.legendmagic.network;

import com.delphy.legendmagic.LegendMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(LegendMagic.MODID, "main"),
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

        // 2. 習得魔法同期パケット (Server -> Client)
        CHANNEL.registerMessage(
                packetId++,
                SyncLearnedSpellsPacket.class,
                SyncLearnedSpellsPacket::encode,
                SyncLearnedSpellsPacket::decode,
                SyncLearnedSpellsPacket::handle
        );

        // 3. 魔導書にセットするパケット (Client -> Server)
        CHANNEL.registerMessage(
                packetId++,
                C2SSetSpellPacket.class,
                C2SSetSpellPacket::encode,
                C2SSetSpellPacket::decode,
                C2SSetSpellPacket::handle
        );
    }

    /**
     * サーバーへパケットを送信する汎用メソッド
     */
    public static void sendToServer(Object packet) {
        if (CHANNEL != null) {
            CHANNEL.sendToServer(packet);
        }
    }

    /**
     * 特定のプレイヤー（クライアント）へパケットを送信する汎用メソッド
     */
    public static void sendToClient(Object packet, ServerPlayer player) {
        if (CHANNEL != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

}