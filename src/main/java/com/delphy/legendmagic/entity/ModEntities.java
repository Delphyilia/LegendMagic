package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.LegendMagic;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = LegendMagic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LegendMagic.MODID);

    public static final RegistryObject<EntityType<RolandSoldierEntity>> ROLAND_SOLDIER =
            ENTITIES.register("rolandsoldier",
                    () -> EntityType.Builder
                            .of(RolandSoldierEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("rolandsoldier")
            );

    public static final RegistryObject<EntityType<DarkWolfEntity>> DARK_WOLF =
            ENTITIES.register("dark_wolf",
                    () -> EntityType.Builder
                            .of(DarkWolfEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build("dark_wolf")
            );

    public static final RegistryObject<EntityType<ThunderWolfEntity>> THUNDER_WOLF =
            ENTITIES.register("thunder_wolf",
                    () -> EntityType.Builder
                            .of(ThunderWolfEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build("thunder_wolf")
            );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    // Attribute登録（旧ModEntityAttributesから統合）
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ROLAND_SOLDIER.get(), RolandSoldierEntity.createAttributes().build());
        event.put(DARK_WOLF.get(), DarkWolfEntity.createAttributes().build());
        event.put(THUNDER_WOLF.get(), ThunderWolfEntity.createAttributes().build());
    }
}
