package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.magic.estabul.SpiritBeast;
import com.delphy.legendmagic.magic.roland.Izuchi;
import com.delphy.legendmagic.magic.runa.SukuinoIkazuchiwo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SpellRegistry {
    // 1. レジストリキーの定義
    public static final ResourceKey<Registry<AbstractMagic>> SPELL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(LegendMagic.MODID, "spells"));

    // 2. DeferredRegisterの作成
    public static final DeferredRegister<AbstractMagic> SPELLS =
            DeferredRegister.create(SPELL_REGISTRY_KEY, LegendMagic.MODID);

    // 3. レジストリ本体（Supplierとして保持）
    public static final Supplier<IForgeRegistry<AbstractMagic>> REGISTRY =
            SPELLS.makeRegistry(() -> new RegistryBuilder<AbstractMagic>().disableSaving());

    public static void register(IEventBus eventBus) {
        // 4. ここで各魔法クラスを登録（IDが spirit_beast であることを確認）
        SPELLS.register("izuchi", Izuchi::new);
        SPELLS.register("spirit_beast", SpiritBeast::new);
        SPELLS.register("sukuino_ikazuchiwo", SukuinoIkazuchiwo::new);

        // 5. Forgeのイベントバスに登録
        SPELLS.register(eventBus);

        System.out.println("LegendMagic: 魔法レジストリをイベントバスに登録しました。");
    }
}