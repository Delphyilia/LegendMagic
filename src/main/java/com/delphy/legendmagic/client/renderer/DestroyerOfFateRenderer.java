package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.DestroyerOfFateEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DestroyerOfFateRenderer
        extends HumanoidMobRenderer<DestroyerOfFateEntity, HumanoidModel<DestroyerOfFateEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/destroyer_of_fate.png"
            );

    public DestroyerOfFateRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                0.6f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(DestroyerOfFateEntity entity) {
        return TEXTURE;
    }
}
