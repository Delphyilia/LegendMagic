package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * クライアント側の詠唱状態を管理するクラス。
 * 詠唱の開始・進行・完了・キャンセルを制御する。
 */
public class CastingManager {
    private static Spell castingSpell = null;
    private static int elapsedTicks = 0;
    private static int requiredTicks = 0;

    /**
     * 魔法の詠唱を開始する。同じ魔法を再度押した場合はキャンセル。
     */
    public static void startCasting(Spell spell) {
        if (spell == null) return;

        // すでに同じ魔法を唱えていたらキャンセル
        if (castingSpell == spell) {
            cancel();
            return;
        }

        // マナ不足チェック (クライアント側即時判定)
        if (com.delphy.legendmagic.client.ClientManaData.getMana() < spell.getManaCost()) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§cマナが足りません！ (必要: " + spell.getManaCost() + ")"), true);
            }
            return;
        }

        // 上位魔法（完全無詠唱・即発動）の場合
        if (spell.getCastTime() <= 0) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSpellPacket(spell.getSpellId(), false));
            return;
        }

        castingSpell = spell;
        elapsedTicks = 0;
        requiredTicks = spell.getCastTime();

        // サーバーへ「詠唱開始」を通知
        ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSpellPacket(spell.getSpellId(), true));
    }

    /**
     * 毎tickの更新処理。詠唱中エフェクトの実行と詠唱完了判定を行う。
     */
    public static void tick() {
        if (castingSpell == null) return;

        elapsedTicks++;

        // 各魔法固有の詠唱中エフェクトおよび足元の回転幾何学魔法陣を実行
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            castingSpell.onCastingTick(player);
            spawnMagicCircleParticles(player, castingSpell);
        }

        // 詠唱完了
        if (elapsedTicks >= requiredTicks) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSpellPacket(castingSpell.getSpellId(), false));
            castingSpell = null;
        }
    }

    private static void spawnMagicCircleParticles(Player player, Spell spell) {
        if (player.level() == null) return;

        double radius = 1.4D;
        int points = 8;
        double rotOffset = elapsedTicks * 0.12D;

        for (int i = 0; i < points; i++) {
            double angle = rotOffset + (i * Math.PI * 2.0D / points);
            double px = player.getX() + Math.cos(angle) * radius;
            double pz = player.getZ() + Math.sin(angle) * radius;
            double py = player.getY() + 0.05D;

            net.minecraft.core.particles.ParticleOptions particle = net.minecraft.core.particles.ParticleTypes.CRIT;
            if (spell.getMagicSystem() == com.delphy.legendmagic.api.MagicSystem.ROLAND) {
                particle = net.minecraft.core.particles.ParticleTypes.FLAME;
            } else if (spell.getMagicSystem() == com.delphy.legendmagic.api.MagicSystem.ESTABUL) {
                particle = net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME;
            } else if (spell.getMagicSystem() == com.delphy.legendmagic.api.MagicSystem.RUNA) {
                particle = net.minecraft.core.particles.ParticleTypes.GLOW;
            }

            player.level().addParticle(particle, px, py, pz, 0.0D, 0.01D, 0.0D);
        }
    }

    public static void cancel() {
        castingSpell = null;
        elapsedTicks = 0;
    }

    public static boolean isCasting() { return castingSpell != null; }
    public static Spell getCastingSpell() { return castingSpell; }
    public static float getProgress() { return (float) elapsedTicks / requiredTicks; }
    public static int getRemainingTicks() { return requiredTicks - elapsedTicks; }
}