package com.delphy.legendmagic.network;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * MODのネットワーク通信を一元管理するクラス。
 * チャネル登録・パケット定義・送信ヘルパーを全てここに集約する。
 */
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
                CastSpellPacket.class,
                CastSpellPacket::encode,
                CastSpellPacket::decode,
                CastSpellPacket::handle
        );

        // 2. 魔法データ同期パケット (Server -> Client)
        CHANNEL.registerMessage(
                packetId++,
                SyncSpellDataPacket.class,
                SyncSpellDataPacket::encode,
                SyncSpellDataPacket::decode,
                SyncSpellDataPacket::handle
        );

        // 3. スロットセットパケット (Client -> Server)
        CHANNEL.registerMessage(
                packetId++,
                SetSpellSlotPacket.class,
                SetSpellSlotPacket::encode,
                SetSpellSlotPacket::decode,
                SetSpellSlotPacket::handle
        );
    }

    // ========== 送信ヘルパー ==========

    /**
     * サーバーへパケットを送信する
     */
    public static void sendToServer(Object packet) {
        if (CHANNEL != null) {
            CHANNEL.sendToServer(packet);
        }
    }

    /**
     * 特定のプレイヤー（クライアント）へパケットを送信する
     */
    public static void sendToClient(Object packet, ServerPlayer player) {
        if (CHANNEL != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    // ========== パケット定義 ==========

    /**
     * 魔法の詠唱開始 / 発動を通知するパケット (Client → Server)
     */
    public static class CastSpellPacket {
        private final ResourceLocation spellId;
        private final boolean isStart; // true=詠唱開始, false=発動

        public CastSpellPacket(ResourceLocation spellId, boolean isStart) {
            this.spellId = spellId;
            this.isStart = isStart;
        }

        public static void encode(CastSpellPacket msg, FriendlyByteBuf buf) {
            buf.writeResourceLocation(msg.spellId);
            buf.writeBoolean(msg.isStart);
        }

        public static CastSpellPacket decode(FriendlyByteBuf buf) {
            return new CastSpellPacket(buf.readResourceLocation(), buf.readBoolean());
        }

        public static void handle(CastSpellPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;

                Spell spell = SpellRegistry.REGISTRY.get().getValue(msg.spellId);
                if (spell != null) {
                    if (msg.isStart) {
                        spell.announceChant(player);
                    } else {
                        spell.execute(player);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 魔法データ（習得・装備）をクライアントに同期するパケット (Server → Client)
     */
    public static class SyncSpellDataPacket {
        private final CompoundTag data;

        public SyncSpellDataPacket(CompoundTag data) {
            this.data = data;
        }

        public static void encode(SyncSpellDataPacket msg, FriendlyByteBuf buf) {
            buf.writeNbt(msg.data);
        }

        public static SyncSpellDataPacket decode(FriendlyByteBuf buf) {
            return new SyncSpellDataPacket(buf.readNbt());
        }

        public static void handle(SyncSpellDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.getPersistentData().merge(msg.data);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 魔導書でスロットに魔法をセットするパケット (Client → Server)
     */
    public static class SetSpellSlotPacket {
        private final int slot;
        private final ResourceLocation spellId;

        public SetSpellSlotPacket(int slot, ResourceLocation spellId) {
            this.slot = slot;
            this.spellId = spellId;
        }

        public static void encode(SetSpellSlotPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.slot);
            buf.writeResourceLocation(msg.spellId);
        }

        public static SetSpellSlotPacket decode(FriendlyByteBuf buf) {
            return new SetSpellSlotPacket(buf.readInt(), buf.readResourceLocation());
        }

        public static void handle(SetSpellSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Spell spell = SpellRegistry.REGISTRY.get().getValue(msg.spellId);
                    if (spell != null) {
                        SpellManager.setSpellAt(player, msg.slot, spell);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}