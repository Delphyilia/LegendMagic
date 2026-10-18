package com.delphy.legendmagic;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = LegendMagic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue LIGHTNING_COOLDOWN =
            BUILDER.comment("Lightning spell cooldown (ticks)")
                    .defineInRange("lightningCooldown", 100, 0, 6000);

    public static final ForgeConfigSpec.DoubleValue GASTARK_RELIC_DROP_CHANCE =
            BUILDER.comment("Base drop chance of Relic Ring (Thunder / Dark) from Gastark Soldier (0.01 = 1%)")
                    .defineInRange("gastarkRelicDropChance", 0.015D, 0.0D, 1.0D);

    public static final ForgeConfigSpec.DoubleValue GASTARK_RELIC_LOOTING_BONUS =
            BUILDER.comment("Additional drop chance per looting level (0.005 = +0.5% per level)")
                    .defineInRange("gastarkRelicLootingBonus", 0.005D, 0.0D, 1.0D);

    // ===== マナシステム設定 =====
    public static final ForgeConfigSpec.DoubleValue DEFAULT_MAX_MANA =
            BUILDER.comment("Default initial maximum mana for players")
                    .defineInRange("defaultMaxMana", 10000.0D, 10.0D, 1000000.0D);

    public static final ForgeConfigSpec.DoubleValue MAX_MANA_CAP =
            BUILDER.comment("Maximum achievable mana cap through spell usage growth")
                    .defineInRange("maxManaCap", 50000.0D, 100.0D, 10000000.0D);

    public static final ForgeConfigSpec.DoubleValue MANA_REGEN_PER_SECOND =
            BUILDER.comment("Mana naturally regenerated per second")
                    .defineInRange("manaRegenPerSecond", 50.0D, 0.0D, 10000.0D);

    public static final ForgeConfigSpec.DoubleValue MANA_GROWTH_RATE =
            BUILDER.comment("Mana growth rate per spell cast (fraction of consumed mana added to max mana, 0.01 = 1%)")
                    .defineInRange("manaGrowthRate", 0.01D, 0.0D, 1.0D);

    // ===== 魔眼システム設定 =====
    public static final ForgeConfigSpec.DoubleValue ALPHA_STIGMA_DAMAGE_MULTIPLIER =
            BUILDER.comment("Damage multiplier granted by Alpha Stigma (default 2.0 = 200%)")
                    .defineInRange("alphaStigmaDamageMultiplier", 2.0D, 1.0D, 10.0D);

    public static final ForgeConfigSpec.DoubleValue IINO_DOUE_CONVERSION_RATE =
            BUILDER.comment("Awakening duration seconds gained per 10 absorbed damage points (default 5.0 seconds)")
                    .defineInRange("iinoDoueConversionRate", 5.0D, 1.0D, 30.0D);

    public static final ForgeConfigSpec.DoubleValue IINO_DOUE_MAX_DURATION =
            BUILDER.comment("Maximum duration in seconds for Iino Doue awakening")
                    .defineInRange("iinoDoueMaxDuration", 60.0D, 10.0D, 300.0D);

    // ===== ボス「運命の破壊者」設定 =====
    public static final ForgeConfigSpec.IntValue DESTROYER_VOID_FALLBACK_Y =
            BUILDER.comment("Y coordinate threshold for Destroyer of Fate void teleport recovery")
                    .defineInRange("destroyerVoidFallbackY", -50, -100, 0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 設定ロード時（今は何もしなくてOK）
    }
}
