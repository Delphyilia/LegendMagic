package com.delphy.legendmagic.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.delphy.legendmagic.magic.SelfStrengtheningSpell;

import java.util.function.Supplier;

public class CastSelfStrengtheningPacket {

    // ===== エンコード =====
    public static void encode(CastSelfStrengtheningPacket msg, FriendlyByteBuf buf) {}

    // ===== デコード =====
    public static CastSelfStrengtheningPacket decode(FriendlyByteBuf buf) {
        return new CastSelfStrengtheningPacket();
    }

    // ===== 処理 =====
    public static void handle(CastSelfStrengtheningPacket msg,
                              Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            SelfStrengtheningSpell.cast(player);
        });

        ctx.get().setPacketHandled(true);
    }
}
