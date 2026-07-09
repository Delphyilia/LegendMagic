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

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    // Attribute登録（旧ModEntityAttributesから統合）
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ROLAND_SOLDIER.get(), RolandSoldierEntity.createAttributes().build());
    }
}
