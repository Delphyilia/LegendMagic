package com.delphy.legendmagic.network;

import com.delphy.legendmagic.LegendMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(LegendMagic.MODID, "network"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // CastMagicPacket側の修正（ID送信）に対応
        CHANNEL.registerMessage(
                packetId++,
                CastMagicPacket.class,
                CastMagicPacket::encode,
                CastMagicPacket::decode,
                CastMagicPacket::handle
        );
    }

    /**
     * 魔法発動パケットをサーバーへ送信
     * @param spellId 魔法の固有ID (例: legendmagic:izuchi)
     */
    public static void sendCastMagic(ResourceLocation spellId) {
        CHANNEL.sendToServer(new CastMagicPacket(spellId));
    }
}