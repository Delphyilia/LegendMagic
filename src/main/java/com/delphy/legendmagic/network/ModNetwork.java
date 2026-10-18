package com.delphy.legendmagic.network;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

        // 4. マナ同期パケット (Server -> Client)
        CHANNEL.registerMessage(
                packetId++,
                SyncManaPacket.class,
                SyncManaPacket::encode,
                SyncManaPacket::decode,
                SyncManaPacket::handle
        );

        // 5. 選択スロット変更パケット (Client -> Server)
        CHANNEL.registerMessage(
                packetId++,
                SelectSpellIndexPacket.class,
                SelectSpellIndexPacket::encode,
                SelectSpellIndexPacket::decode,
                SelectSpellIndexPacket::handle
        );

        // 6. 殲滅眼蓄積魔力同期パケット (Server -> Client)
        CHANNEL.registerMessage(
                packetId++,
                SyncIinoDouePacket.class,
                SyncIinoDouePacket::encode,
                SyncIinoDouePacket::decode,
                SyncIinoDouePacket::handle
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

                // 1. 殲滅眼（イーノ・ドゥーエ）チェック: 魔法使用不可＆アクティブ能動覚醒
                if (com.delphy.legendmagic.util.EyeUtil.hasIinoDoue(player)) {
                    if (!msg.isStart) {
                        float storedDamage = com.delphy.legendmagic.util.EyeUtil.consumeIinoDouePool(player);
                        if (storedDamage <= 0.0F) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c【殲滅眼】喰らった魔法の力が足りない……（敵の魔法を吸収してください）"), true);
                            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
                            return;
                        }

                        // 蓄積10ダメージごとに +5秒 (100 ticks)、最大60秒 (1200 ticks)
                        int durationTicks = Math.min(1200, Math.max(100, (int) ((storedDamage / 10.0F) * 100)));
                        int durationSec = durationTicks / 20;

                        // 能動覚醒発動！
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§4「力を喰らい……それを放つ」"), true);

                        // バフ付与（吸収、攻撃力、速度、耐性）
                        int absorptionAmp = storedDamage >= 40.0F ? 3 : (storedDamage >= 20.0F ? 2 : 1);
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.ABSORPTION, durationTicks, absorptionAmp, false, true, true));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, durationTicks, 1, false, true, true)); // Strength II
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, durationTicks, 1, false, true, true)); // Speed II
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, durationTicks, 1, false, true, true)); // Resistance II

                        // 覚醒演出
                        ServerLevel sLevel = player.serverLevel();
                        sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                                player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.8D, 0.8D, 0.8D, 0.15D);
                        sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH,
                                player.getX(), player.getY() + 1.0D, player.getZ(), 2, 0, 0, 0, 0);
                        sLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.PLAYERS, 1.2F, 1.6F);
                        sLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.WARDEN_ROAR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.3F);

                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                String.format("§6【殲滅眼覚醒】蓄積 %.1f ダメージを解放！ %d秒間、暴虐の力を獲得！", storedDamage, durationSec)));
                    }
                    return;
                }

                // 2. 通常の魔法処理
                Spell spell = SpellRegistry.REGISTRY.get().getValue(msg.spellId);
                if (spell != null) {
                    if (msg.isStart) {
                        if (com.delphy.legendmagic.magic.ManaManager.getMana(player) < spell.getManaCost()) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cマナが足りません！ (必要: " + spell.getManaCost() + ")"), true);
                            return;
                        }
                        spell.announceChant(player);
                    } else {
                        if (com.delphy.legendmagic.magic.ManaManager.consumeMana(player, spell.getManaCost())) {
                            spell.execute(player);

                            // 3. 複写眼（アルファ・スティグマ）による他者魔法のコピー・解析習得処理
                            checkAndLearnFromSight(player, spell);
                        } else {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cマナが足りません！"), true);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        /**
         * 術者が放った魔法を、視界内にいる複写眼保持者が解析・即時習得する
         */
        public static void checkAndLearnFromSight(net.minecraft.world.entity.LivingEntity caster, Spell spell) {
            if (!(caster.level() instanceof ServerLevel level)) return;

            double radius = 32.0D;
            net.minecraft.world.phys.AABB lookBox = caster.getBoundingBox().inflate(radius);
            java.util.List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, lookBox,
                    p -> p != caster && com.delphy.legendmagic.util.EyeUtil.hasAlphaStigma(p));

            for (ServerPlayer observer : nearbyPlayers) {
                if (!com.delphy.legendmagic.magic.SpellManager.hasLearned(observer, spell)) {
                    if (observer.hasLineOfSight(caster)) {
                        com.delphy.legendmagic.magic.SpellManager.learnSpell(observer, spell);

                        observer.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§c【複写眼】魔法式を解析・習得した: §e" + spell.getName() + " §7(" + spell.getChant() + ")"), false);
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                                observer.getX(), observer.getY() + 1.5D, observer.getZ(), 25, 0.4D, 0.4D, 0.4D, 0.1D);
                        level.playSound(null, observer.getX(), observer.getY(), observer.getZ(),
                                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.8F);
                    }
                }
            }
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

    /**
     * プレイヤーのマナをクライアントに同期するパケット (Server → Client)
     */
    public static class SyncManaPacket {
        private final float mana;
        private final float maxMana;

        public SyncManaPacket(float mana, float maxMana) {
            this.mana = mana;
            this.maxMana = maxMana;
        }

        public static void encode(SyncManaPacket msg, FriendlyByteBuf buf) {
            buf.writeFloat(msg.mana);
            buf.writeFloat(msg.maxMana);
        }

        public static SyncManaPacket decode(FriendlyByteBuf buf) {
            return new SyncManaPacket(buf.readFloat(), buf.readFloat());
        }

        public static void handle(SyncManaPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                com.delphy.legendmagic.client.ClientManaData.set(msg.mana, msg.maxMana);
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * ラジアルメニュー等から選択スロットインデックスを変更するパケット (Client → Server)
     */
    public static class SelectSpellIndexPacket {
        private final int index;

        public SelectSpellIndexPacket(int index) {
            this.index = index;
        }

        public static void encode(SelectSpellIndexPacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.index);
        }

        public static SelectSpellIndexPacket decode(FriendlyByteBuf buf) {
            return new SelectSpellIndexPacket(buf.readInt());
        }

        public static void handle(SelectSpellIndexPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    player.getPersistentData().putInt(SpellManager.TAG_SELECTED_INDEX, msg.index);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * 殲滅眼（イーノ・ドゥーエ）の蓄積プール値をクライアントに同期するパケット (Server → Client)
     */
    public static class SyncIinoDouePacket {
        private final float pool;

        public SyncIinoDouePacket(float pool) {
            this.pool = pool;
        }

        public static void encode(SyncIinoDouePacket msg, FriendlyByteBuf buf) {
            buf.writeFloat(msg.pool);
        }

        public static SyncIinoDouePacket decode(FriendlyByteBuf buf) {
            return new SyncIinoDouePacket(buf.readFloat());
        }

        public static void handle(SyncIinoDouePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                com.delphy.legendmagic.client.ClientIinoDoueData.setPool(msg.pool);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}