package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.client.renderer.RolandSoldierRenderer;
import com.delphy.legendmagic.client.renderer.DarkWolfRenderer;
import com.delphy.legendmagic.client.renderer.ThunderWolfRenderer;
import com.delphy.legendmagic.client.renderer.DestroyerOfFateRenderer;
import com.delphy.legendmagic.client.renderer.RolandGuardCaptainRenderer;

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
        EntityRenderers.register(
                ModEntities.DESTROYER_OF_FATE.get(),
                DestroyerOfFateRenderer::new
        );
        EntityRenderers.register(
                ModEntities.ROLAND_GUARD_CAPTAIN.get(),
                RolandGuardCaptainRenderer::new
        );
        EntityRenderers.register(
                ModEntities.RUNA_PRIEST.get(),
                com.delphy.legendmagic.client.renderer.RunaPriestRenderer::new
        );
        EntityRenderers.register(
                ModEntities.ESTABUL_SOLDIER.get(),
                com.delphy.legendmagic.client.renderer.EstabulSoldierRenderer::new
        );
        EntityRenderers.register(
                ModEntities.GASTARK_SOLDIER.get(),
                com.delphy.legendmagic.client.renderer.GastarkSoldierRenderer::new
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
