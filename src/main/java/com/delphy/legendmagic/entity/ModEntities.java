package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.LegendMagic;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LegendMagic.MODID);

    public static final RegistryObject<EntityType<RolandSoldierEntity>> ROLANDSOLDIER =
            ENTITIES.register("rolandsoldier",
                    () -> EntityType.Builder
                            .of(RolandSoldierEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("rolandsoldier")
            );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
