package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.RolandGuardCaptainEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RolandGuardCaptainRenderer
        extends HumanoidMobRenderer<RolandGuardCaptainEntity, HumanoidModel<RolandGuardCaptainEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    LegendMagic.MODID,
                    "textures/entity/roland_guard_captain.png"
            );

    public RolandGuardCaptainRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                0.55f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(RolandGuardCaptainEntity entity) {
        return TEXTURE;
    }
}
