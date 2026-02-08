package com.delphy.legendmagic;

import com.delphy.legendmagic.client.input.ModKeyBindings; // 追加
import com.delphy.legendmagic.create.ModCreativeTabs;
import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.item.LegendMagicItems;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist; // 追加
import net.minecraftforge.client.event.RegisterKeyMappingsEvent; // 追加
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor; // 追加
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

        ModNetwork.register();

        LegendMagicItems.register(bus);
        ModCreativeTabs.CREATIVE_TABS.register(bus);
        ModEntities.ENTITIES.register(bus);

        // --- ここから追加 ---
        // クライアント側（シングルプレイ・マルチ参加時）のみ、キー入力を登録する
        // () -> () -> という二重のラムダにすることで、サーバー側でクラスがロードされるのを防ぎます
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(this::registerClientInputs);
        });
        // --- ここまで追加 ---

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