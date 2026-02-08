package com.delphy.legendmagic.create;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.item.LegendMagicItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LegendMagic.MODID);

    public static final RegistryObject<CreativeModeTab> LEGENDMAGIC_TAB =
            CREATIVE_TABS.register("legendmagic_tab", () ->
                    CreativeModeTab.builder()
                            .icon(() -> new ItemStack(LegendMagicItems.ALPHA_STIGMA.get()))
                            .title(Component.literal("Legend Magic"))
                            .displayItems((parameters, output) -> {
                                output.accept(LegendMagicItems.ALPHA_STIGMA.get());
                                output.accept(LegendMagicItems.ROLAND_SOLDIER_SPAWN_EGG.get());
                                output.accept(LegendMagicItems.DANGO_DOUGH.get());
                                output.accept(LegendMagicItems.DANGO.get());
                                output.accept(LegendMagicItems.DANGO_SEAL.get());
                                output.accept(LegendMagicItems.LONG_SWORD.get());
                                output.accept(LegendMagicItems.GOD_DANGO_SWORD.get());
                            })
                            .build()
            );
}
