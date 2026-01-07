package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.RolandSoldierEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RolandSoldierRenderer
        extends HumanoidMobRenderer<RolandSoldierEntity, HumanoidModel<RolandSoldierEntity>> {

    @SuppressWarnings("remuval")
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/roland_soldier.png"
            );

    public RolandSoldierRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                0.5f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(RolandSoldierEntity entity) {
        return TEXTURE;
    }
}
