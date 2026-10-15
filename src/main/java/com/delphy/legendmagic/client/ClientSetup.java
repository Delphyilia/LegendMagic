package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.client.renderer.RolandSoldierRenderer;
import com.delphy.legendmagic.client.renderer.DarkWolfRenderer;

import com.delphy.legendmagic.client.renderer.ThunderWolfRenderer;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = LegendMagic.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        // ⭐ エンティティレンダラー登録
        EntityRenderers.register(
                ModEntities.ROLAND_SOLDIER.get(),
                RolandSoldierRenderer::new
        );
        EntityRenderers.register(
                ModEntities.DARK_WOLF.get(),
                DarkWolfRenderer::new
        );
        EntityRenderers.register(
                ModEntities.THUNDER_WOLF.get(),
                ThunderWolfRenderer::new
        );

        // ⭐ ブロックエンティティレンダラー登録（浮遊アイテム描画）
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                com.delphy.legendmagic.block.entity.ModBlockEntities.RELIC_ALTAR_BE.get(),
                com.delphy.legendmagic.client.renderer.RelicAltarRenderer::new
        );
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                com.delphy.legendmagic.block.entity.ModBlockEntities.OFFERING_PEDESTAL_BE.get(),
                com.delphy.legendmagic.client.renderer.OfferingPedestalRenderer::new
        );
    }
}
