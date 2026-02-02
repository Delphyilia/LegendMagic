package com.delphy.legendmagic.network;

import com.delphy.legendmagic.LegendMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static SimpleChannel CHANNEL;

    // ===== 登録 =====
    public static void register() {

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(LegendMagic.MODID, "network"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CHANNEL.registerMessage(
                packetId++,
                CastLightningPacket.class,
                CastLightningPacket::encode,
                CastLightningPacket::decode,
                CastLightningPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                CastSelfStrengtheningPacket.class,
                CastSelfStrengtheningPacket::encode,
                CastSelfStrengtheningPacket::decode,
                CastSelfStrengtheningPacket::handle
        );
    }

    // ===== クライアント → サーバー =====
    public static void sendLightningCast() {
        if (Minecraft.getInstance().player == null) return;

        CHANNEL.sendToServer(new CastLightningPacket());
    }

    public static void sendSelfStrengtheningCast() {
        if (Minecraft.getInstance().player == null) return;

        CHANNEL.sendToServer(new CastSelfStrengtheningPacket());
    }
}
