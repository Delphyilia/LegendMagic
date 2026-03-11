package com.delphy.legendmagic.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncLearnedSpellsPacket {
    private final CompoundTag data;

    public SyncLearnedSpellsPacket(CompoundTag data) {
        this.data = data;
    }

    public static void encode(SyncLearnedSpellsPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SyncLearnedSpellsPacket decode(FriendlyByteBuf buf) {
        return new SyncLearnedSpellsPacket(buf.readNbt());
    }

    public static void handle(SyncLearnedSpellsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // クライアント側のプレイヤーのNBTを直接上書きして同期する
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getPersistentData().merge(msg.data);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}