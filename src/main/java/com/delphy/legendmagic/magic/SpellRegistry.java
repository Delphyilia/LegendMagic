package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.magic.estabul.LightSpiritBeast;
import com.delphy.legendmagic.magic.estabul.SkyDancingBeast;
import com.delphy.legendmagic.magic.estabul.SpiritBeast;
import com.delphy.legendmagic.magic.estabul.VortexOfSpirits;
import com.delphy.legendmagic.magic.roland.Chigashira;
import com.delphy.legendmagic.magic.roland.Izuchi;
import com.delphy.legendmagic.magic.runa.SukuinoHonoowo;
import com.delphy.legendmagic.magic.runa.SukuinoIkazuchiwo;
import com.delphy.legendmagic.magic.runa.SukuinoKazewo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SpellRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    // レジストリキーの定義
    public static final ResourceKey<Registry<Spell>> SPELL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(LegendMagic.MODID, "spells"));

    // DeferredRegisterの作成
    public static final DeferredRegister<Spell> SPELLS =
            DeferredRegister.create(SPELL_REGISTRY_KEY, LegendMagic.MODID);

    // レジストリ本体（Supplierとして保持）
    public static final Supplier<IForgeRegistry<Spell>> REGISTRY =
            SPELLS.makeRegistry(() -> new RegistryBuilder<Spell>().disableSaving());

    public static void register(IEventBus eventBus) {
        // ローランド魔法体系
        SPELLS.register("izuchi", Izuchi::new);
        SPELLS.register("chigashira", Chigashira::new);
        SPELLS.register("izuchi_death", com.delphy.legendmagic.magic.roland.IzuchiDeath::new);
        SPELLS.register("kurenai", com.delphy.legendmagic.magic.roland.Kurenai::new);
        SPELLS.register("misumi", com.delphy.legendmagic.magic.roland.Misumi::new);
        SPELLS.register("fuen", com.delphy.legendmagic.magic.roland.Fuen::new);
        SPELLS.register("bakushu", com.delphy.legendmagic.magic.roland.Bakushu::new);
        SPELLS.register("shira", com.delphy.legendmagic.magic.roland.Shira::new);
        SPELLS.register("ante", com.delphy.legendmagic.magic.roland.Ante::new);

        // ルーナ魔法体系
        SPELLS.register("sukuino_kazewo", SukuinoKazewo::new);
        SPELLS.register("sukuino_honoowo", SukuinoHonoowo::new);
        SPELLS.register("sukuino_ikazuchiwo", SukuinoIkazuchiwo::new);

        // エスタブール魔法体系
        SPELLS.register("spirit_beast", SpiritBeast::new);
        SPELLS.register("light_spirit_beast", LightSpiritBeast::new);
        SPELLS.register("sky_dancing_beast", SkyDancingBeast::new);
        SPELLS.register("vortex_of_spirits", VortexOfSpirits::new);

        // Forgeのイベントバスに登録
        SPELLS.register(eventBus);

        LOGGER.info("LegendMagic: Spell registry registered to event bus.");
    }
}