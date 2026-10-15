package com.delphy.legendmagic.block.entity;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LegendMagic.MODID);

    public static final RegistryObject<BlockEntityType<RelicAltarBlockEntity>> RELIC_ALTAR_BE =
            BLOCK_ENTITIES.register("relic_altar", () ->
                    BlockEntityType.Builder.of(RelicAltarBlockEntity::new,
                            ModBlocks.RELIC_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<OfferingPedestalBlockEntity>> OFFERING_PEDESTAL_BE =
            BLOCK_ENTITIES.register("offering_pedestal", () ->
                    BlockEntityType.Builder.of(OfferingPedestalBlockEntity::new,
                            ModBlocks.OFFERING_PEDESTAL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
