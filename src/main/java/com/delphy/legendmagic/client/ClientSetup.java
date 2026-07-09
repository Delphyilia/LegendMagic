package com.delphy.legendmagic.client;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.client.renderer.RolandSoldierRenderer;

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


    }
}
