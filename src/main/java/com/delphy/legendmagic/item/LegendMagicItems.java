package com.delphy.legendmagic.item;

import com.delphy.legendmagic.LegendMagic;

import com.delphy.legendmagic.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LegendMagicItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LegendMagic.MODID);

    public static final RegistryObject<Item> ALPHA_STIGMA = ITEMS.register(
            "alpha_stigma",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> ROLAND_SOLDIER_SPAWN_EGG =
            ITEMS.register("roland_soldier_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.ROLANDSOLDIER,
                            0x3A3A3A,
                            0xAAAAAA,
                            new Item.Properties()
                    )
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
