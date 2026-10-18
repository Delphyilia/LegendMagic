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
                            0xAAAAAA, // 斑点の色
                            new Item.Properties()
                    )
            );

    // ローランド近衛隊長のスポーンエッグ（中ボス）
    public static final RegistryObject<Item> ROLAND_GUARD_CAPTAIN_SPAWN_EGG =
            ITEMS.register("roland_guard_captain_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.ROLAND_GUARD_CAPTAIN,
                            0x8B0000, // 深紅
                            0xFFD700, // 金
                            new Item.Properties()
                    )
            );

    // ルーナ神官のスポーンエッグ（友好Mob）
    public static final RegistryObject<Item> RUNA_PRIEST_SPAWN_EGG =
            ITEMS.register("runa_priest_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.RUNA_PRIEST,
                            0xE0EEE0, // 白緑
                            0x00CED1, // エメラルドシアン
                            new Item.Properties()
                    )
            );

    // エスタブール魔導兵のスポーンエッグ
    public static final RegistryObject<Item> ESTABUL_SOLDIER_SPAWN_EGG =
            ITEMS.register("estabul_soldier_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.ESTABUL_SOLDIER,
                            0x1A237E, // インディゴ
                            0x00E5FF, // シアンブルー
                            new Item.Properties()
                    )
            );

    // ガスターク兵のスポーンエッグ（遺物所持エリート兵）
    public static final RegistryObject<Item> GASTARK_SOLDIER_SPAWN_EGG =
            ITEMS.register("gastark_soldier_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.GASTARK_SOLDIER,
                            0x2E0854, // ダークパープル
                            0xDAA520, // アンティークゴールド
                            new Item.Properties()
                    )
            );

    // 運命の破壊者のスポーンエッグ（ボス）
    public static final RegistryObject<Item> DESTROYER_OF_FATE_SPAWN_EGG =
            ITEMS.register("destroyer_of_fate_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.DESTROYER_OF_FATE,
                            0x0F0F1A, // 漆黒
                            0xFF1493, // クリムゾンマゼンタ
                            new Item.Properties()
                    )
            );

    // 雷オオカミのスポーンエッグ
    public static final RegistryObject<Item> THUNDER_WOLF_SPAWN_EGG =
            ITEMS.register("thunder_wolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.THUNDER_WOLF,
                            0x1E3F66, // 紺
                            0xFFEA00, // 稲妻イエロー
                            new Item.Properties()
                    )
            );

    // 黒オオカミのスポーンエッグ
    public static final RegistryObject<Item> DARK_WOLF_SPAWN_EGG =
            ITEMS.register("dark_wolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.DARK_WOLF,
                            0x1A1A1A, // 黒
                            0x6A0DAD, // ダークバイオレット
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

    // 雷獣の指輪
    public static final RegistryObject<Item> RING_OF_THUNDER_BEASTS = ITEMS.register("ring_of_thunder_beasts",
            () -> new RingofThunderBeastsItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 遺物の欠片（忘却欠片の進化素材）
    public static final RegistryObject<Item> RELIC_SHARD = ITEMS.register("relic_shard",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));

    // 殲滅眼（イーノ・ドゥーエ）
    public static final RegistryObject<Item> IINO_DOUE = ITEMS.register("iino_doue",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 内面のナイフ（専用ディメンション転移＆ボスリマッチ用キーアイテム）
    public static final RegistryObject<Item> INNER_KNIFE = ITEMS.register("inner_knife",
            () -> new InnerKnifeItem(new Item.Properties()));

    // 破壊者の魔導核（上位魔法「稲死光」解放アイテム）
    public static final RegistryObject<Item> CORE_OF_FATE = ITEMS.register("core_of_fate",
            () -> new CoreOfFateItem(new Item.Properties()));

    // メノリスの羅針盤（新ディメンション「メノリス大陸」転移アイテム）
    public static final RegistryObject<Item> MENORIS_COMPASS = ITEMS.register("menoris_compass",
            () -> new MenorisCompassItem(new Item.Properties()));

    // アイテムを登録
    // LegendMagic.javaで呼び出すことで全アイテムが登録される
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}