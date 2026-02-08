package com.delphy.legendmagic.item;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LegendMagicItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LegendMagic.MODID);

    // --- 既存のアイテム ---
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

    // --- 追加分：だんごシリーズ ---

    // だんご生地（クラフト後にバケツを返す設定）
    public static final RegistryObject<Item> DANGO_DOUGH = ITEMS.register("dango_dough",
            () -> new Item(new Item.Properties()));

    // だんご（食べると再生能力付与）
    public static final RegistryObject<Item> DANGO = ITEMS.register("dango",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationMod(0.3f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0f)
                    .alwaysEat()
                    .build())));

    // だんごシール
    public static final RegistryObject<Item> DANGO_SEAL = ITEMS.register("dango_seal",
            () -> new Item(new Item.Properties()));

    // --- 追加分：武器 ---

    // 長剣（鉄の剣相当、あるいは少し強め）
    public static final RegistryObject<Item> LONG_SWORD = ITEMS.register("long_sword",
            () -> new SwordItem(Tiers.IRON, 4, -2.4f, new Item.Properties()));

    // だんご神の長剣（レアリティをEPICに設定）
    public static final RegistryObject<Item> GOD_DANGO_SWORD = ITEMS.register("god_dango_sword",
            () -> new SwordItem(Tiers.DIAMOND, 10, -2.0f, new Item.Properties().rarity(Rarity.EPIC)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}