package com.delphy.legendmagic.network;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSetSpellPacket {
    private final int slot;
    private final ResourceLocation spellId;

    public C2SSetSpellPacket(int slot, ResourceLocation spellId) {
        this.slot = slot;
        this.spellId = spellId;
    }

    public static void encode(C2SSetSpellPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeResourceLocation(msg.spellId);
    }

    public static C2SSetSpellPacket decode(FriendlyByteBuf buf) {
        return new C2SSetSpellPacket(buf.readInt(), buf.readResourceLocation());
    }

    public static void handle(C2SSetSpellPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                AbstractMagic spell = SpellRegistry.REGISTRY.get().getValue(msg.spellId);
                if (spell != null) {
                    // サーバー側でセット処理を実行
                    SpellManager.setSpellAt(player, msg.slot, spell);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}