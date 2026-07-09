package com.delphy.legendmagic;

import com.delphy.legendmagic.client.input.ModKeyBindings;
import com.delphy.legendmagic.creativetab.ModCreativeTabs;
import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.item.LegendMagicItems;
import com.delphy.legendmagic.magic.SpellRegistry;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LegendMagic.MODID)
public class LegendMagic {

    public static final String MODID = "legendmagic";

    @SuppressWarnings("removal")
    public LegendMagic() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // 魔法レジストリを登録（アイテム登録より先に呼ぶ）
        SpellRegistry.register(bus);

        ModNetwork.register();

        LegendMagicItems.register(bus);
        ModCreativeTabs.CREATIVE_TABS.register(bus);
        ModEntities.ENTITIES.register(bus);

        // クライアント側のみキー入力を登録
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(this::registerClientInputs);
        });

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );
    }

    // クライアント側でのみ実行されるメソッド
    private void registerClientInputs(RegisterKeyMappingsEvent event) {
        ModKeyBindings.init(event);
    }
}