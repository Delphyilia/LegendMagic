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

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 設定ロード時（今は何もしなくてOK）
    }
}
