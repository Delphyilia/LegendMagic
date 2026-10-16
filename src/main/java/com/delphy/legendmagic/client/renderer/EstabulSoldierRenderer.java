package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.EstabulSoldierEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EstabulSoldierRenderer
        extends HumanoidMobRenderer<EstabulSoldierEntity, HumanoidModel<EstabulSoldierEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/estabul_soldier.png"
            );

    public EstabulSoldierRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                0.5f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(EstabulSoldierEntity entity) {
        return TEXTURE;
    }
}
