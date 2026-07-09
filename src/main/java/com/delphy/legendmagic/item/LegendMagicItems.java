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

    // DeferredRegister<Item> ITEMSを使ってアイテムを登録する
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LegendMagic.MODID);

    // アルファ・スティグマ（複写眼）
    public static final RegistryObject<Item> ALPHA_STIGMA = ITEMS.register(
            "alpha_stigma",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    // ローランド兵のスポーンエッグ
    public static final RegistryObject<Item> ROLAND_SOLDIER_SPAWN_EGG =
            ITEMS.register("roland_soldier_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.ROLAND_SOLDIER,
                            0x3A3A3A, // 背景の色
                            0xAAAAAA, // ぶつぶつの色
                            new Item.Properties()
                    )
            );

    // だんご生地
    // クラフト時にバケツを返す設定は不要（クラフト時に自動で返却される）
    public static final RegistryObject<Item> DANGO_DOUGH = ITEMS.register("dango_dough",
            () -> new Item(new Item.Properties()));

    // だんご
    // 食べると再生能力付与（100ティック=、2段階）
    public static final RegistryObject<Item> DANGO = ITEMS.register("dango",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4) // 満腹度を4回復
                    .saturationMod(0.3f) // 隠し満腹度
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0f) // 再生能力付与（100ティック=5秒、2段階）
                    .alwaysEat() // いつでも食べられる
                    .build() // いい感じに設定
    )));

    // だんごシール
    public static final RegistryObject<Item> DANGO_SEAL = ITEMS.register("dango_seal",
            () -> new Item(new Item.Properties()));

    // 長剣（鉄の剣相当、あるいは少し強め）
    public static final RegistryObject<Item> LONG_SWORD = ITEMS.register("long_sword",
            () -> new SwordItem(Tiers.IRON, 4, -2.4f, new Item.Properties()));

    // だんご神の長剣（レアリティをEPICに設定）
    public static final RegistryObject<Item> GOD_DANGO_SWORD = ITEMS.register("god_dango_sword",
            () -> new SwordItem(Tiers.DIAMOND, 10, -2.0f, new Item.Properties().rarity(Rarity.EPIC)));

    // 魔法書
    public static final RegistryObject<Item> GRIMOIRE = ITEMS.register("grimoire",
            () -> new GrimoireItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    // 黒叡の指輪
    public static final RegistryObject<Item> RING_OF_THE_DARK_EMPEROR = ITEMS.register("ring_of_the_dark_emperor",
            () -> new RingOfTheDarkEmperorItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // アイテムを登録
    // LegendMagic.javaで呼び出すことで全アイテムが登録される
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}