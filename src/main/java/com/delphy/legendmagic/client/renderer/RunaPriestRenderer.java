package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.RunaPriestEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RunaPriestRenderer
        extends HumanoidMobRenderer<RunaPriestEntity, HumanoidModel<RunaPriestEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/runa_priest.png"
            );

    public RunaPriestRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                0.5f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(RunaPriestEntity entity) {
        return TEXTURE;
    }
}
