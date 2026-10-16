package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.GastarkSoldierEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GastarkSoldierRenderer
        extends HumanoidMobRenderer<GastarkSoldierEntity, HumanoidModel<GastarkSoldierEntity>> {

    private static final ResourceLocation TEXTURE_THUNDER =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/gastark_soldier_thunder.png"
            );

    private static final ResourceLocation TEXTURE_DARK =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/gastark_soldier_dark.png"
            );

    public GastarkSoldierRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                0.55f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(GastarkSoldierEntity entity) {
        return (entity.getRelicType() == GastarkSoldierEntity.RELIC_THUNDER)
                ? TEXTURE_THUNDER
                : TEXTURE_DARK;
    }
}
