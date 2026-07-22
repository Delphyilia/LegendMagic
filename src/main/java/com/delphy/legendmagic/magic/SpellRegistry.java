package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.magic.estabul.SpiritBeast;
import com.delphy.legendmagic.magic.roland.Izuchi;
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
        // 各魔法クラスを登録
        SPELLS.register("izuchi", Izuchi::new);
        SPELLS.register("spirit_beast", SpiritBeast::new);
        SPELLS.register("sukuino_ikazuchiwo", SukuinoIkazuchiwo::new);
        SPELLS.register("sukuino_kazewo", SukuinoKazewo::new);

        // Forgeのイベントバスに登録
        SPELLS.register(eventBus);

        LOGGER.info("LegendMagic: Spell registry registered to event bus.");
    }
}