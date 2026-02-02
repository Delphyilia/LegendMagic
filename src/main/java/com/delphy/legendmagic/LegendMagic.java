package com.delphy.legendmagic;

import com.delphy.legendmagic.create.ModCreativeTabs;
import com.delphy.legendmagic.entity.ModEntities;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import com.delphy.legendmagic.network.ModNetwork;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.delphy.legendmagic.item.LegendMagicItems;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod(LegendMagic.MODID)
public class LegendMagic {

    public static final String MODID = "legendmagic";

    @SuppressWarnings("removal")
    public LegendMagic() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModNetwork.register();

        LegendMagicItems.register(bus);
        ModCreativeTabs.CREATIVE_TABS.register(bus);
        ModEntities.ENTITIES.register(bus);

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );
    }
}


