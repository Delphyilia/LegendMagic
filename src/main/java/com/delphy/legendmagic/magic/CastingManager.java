package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.network.CastMagicPacket;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;

public class CastingManager {
    private static AbstractMagic activeSpell = null;
    private static int castTick = 0;
    private static int maxCastTime = 0;

    public static void startCasting(AbstractMagic spell) {
        if (spell == null) return;

        // すでに同じ魔法を唱えていたらキャンセル
        if (activeSpell == spell) {
            cancel();
            return;
        }

        activeSpell = spell;
        castTick = 0;
        maxCastTime = spell.getCastTime();

        // ⭐ サーバーへ「詠唱開始」を通知（第2引数をtrueに）
        // これによりサーバー側で announceChant が実行されます
        ModNetwork.CHANNEL.sendToServer(new CastMagicPacket(spell.getSpellId(), true));
    }

    public static void tick() {
        if (activeSpell == null) return;

        castTick++;

        // --- 自分の周りに雷を漂わせる演出 (20%の確率で発生) ---
        // いずち限定
        if (Minecraft.getInstance().level.random.nextFloat() < 0.2f) {
            LocalPlayer player = Minecraft.getInstance().player;
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 6;
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 6;

            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
            if (bolt != null) {
                bolt.moveTo(x, player.getY(), z);
                bolt.setVisualOnly(true); // 地形破壊やダメージなし
                player.level().addFreshEntity(bolt);
            }
        }


        // 詠唱完了！
        if (castTick >= maxCastTime) {
            // ⭐ サーバーへ「魔法実行」を通知（第2引数をfalseに）
            ModNetwork.CHANNEL.sendToServer(new CastMagicPacket(activeSpell.getSpellId(), false));
            activeSpell = null;
        }
    }

    public static void cancel() {
        activeSpell = null;
        castTick = 0;
    }

    public static boolean isCasting() { return activeSpell != null; }
    public static AbstractMagic getActiveSpell() { return activeSpell; }
    public static float getProgress() { return (float)castTick / maxCastTime; }
    public static int getRemainingTicks() { return maxCastTime - castTick; }
}