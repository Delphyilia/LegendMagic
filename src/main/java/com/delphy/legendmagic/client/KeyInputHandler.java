package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.magic.SelfStrengtheningSpell;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.MagicType;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

import com.delphy.legendmagic.util.EyeUtil;
import com.delphy.legendmagic.client.input.ModKeyBindings;
import com.delphy.legendmagic.magic.LightningSpell;
import com.delphy.legendmagic.network.ModNetwork;



@Mod.EventBusSubscriber(modid = LegendMagic.MODID, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ===== 魔法発動キー =====
        if (ModKeyBindings.CAST_MAGIC_KEY.consumeClick())
        {

            if (!EyeUtil.hasCopyEye(mc.player)) return;

            MagicType current = SpellManager.getCurrent();

            switch (current) {

                case LIGHTNING -> {

                    mc.player.sendSystemMessage(
                            Component.literal(LightningSpell.CHANT)
                    );

                    ModNetwork.sendLightningCast();
                }

                case SELF_STRENGTHENING -> {

                    mc.player.sendSystemMessage(
                            Component.literal(SelfStrengtheningSpell.CHANT)
                    );

                    ModNetwork.sendSelfStrengtheningCast();
                }
            }
        }

        // ===== 魔法切替キー =====
        if (ModKeyBindings.MAGIC_SWITCH_KEY.consumeClick()) {

            MagicType newMagic = SpellManager.next();

            mc.player.sendSystemMessage(
                    Component.literal("現在の魔法: " + newMagic.getDisplayName())
            );
        }
    }

}
