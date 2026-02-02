package com.delphy.legendmagic.network;

import com.delphy.legendmagic.magic.LightningSpell;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CastLightningPacket {

    public CastLightningPacket() {}

    public static void encode(CastLightningPacket msg, FriendlyByteBuf buf) {}
    public static CastLightningPacket decode(FriendlyByteBuf buf) {
        return new CastLightningPacket();
    }

    public static void handle(CastLightningPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (!EyeUtil.hasCopyEye(player)) {
                return;
            }
            if (player != null) {
                player.getServer().execute(() -> {
                    LightningSpell.cast(player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
