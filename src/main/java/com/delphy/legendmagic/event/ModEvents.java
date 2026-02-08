package com.delphy.legendmagic.event;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.item.LegendMagicItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LegendMagic.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // プレイヤーが死亡した時のみ実行
        if (event.getEntity() instanceof Player player) {

            // メインハンドとオフハンドの両方をチェック
            InteractionHand hand = null;
            if (player.getMainHandItem().is(LegendMagicItems.GOD_DANGO_SWORD.get())) {
                hand = InteractionHand.MAIN_HAND;
            } else if (player.getOffhandItem().is(LegendMagicItems.GOD_DANGO_SWORD.get())) {
                hand = InteractionHand.OFF_HAND;
            }

            // 「だんご神の長剣」を持っていた場合
            if (hand != null) {
                // 1. 死亡をキャンセルして踏みとどまる
                event.setCanceled(true);

                // 2. ステータスを回復・付与
                player.setHealth(1.0F); // 体力をハート半分で固定
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

                // 3. トーテム発動の視覚エフェクトと音
                player.level().broadcastEntityEvent(player, (byte) 35);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                // 4. 【アイテムの退化】神の力を失い、通常の長剣に戻る
                ItemStack godSword = player.getItemInHand(hand);
                ItemStack normalSword = new ItemStack(LegendMagicItems.LONG_SWORD.get());

                // 元の剣の耐久値や名前、エンチャントを引き継ぎたい場合は以下のようにコピー
                if (godSword.hasTag()) {
                    normalSword.setTag(godSword.getTag().copy());
                }

                // アイテムを入れ替える
                player.setItemInHand(hand, normalSword);
            }
        }
    }
}