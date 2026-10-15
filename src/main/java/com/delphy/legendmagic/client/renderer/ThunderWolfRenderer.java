package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class ThunderWolfRenderer extends WolfRenderer {
    private static final ResourceLocation THUNDER_WOLF_TEXTURE = new ResourceLocation(LegendMagic.MODID, "textures/entity/thunder_wolf/thunder_wolf.png");

    public ThunderWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Wolf wolf) {
        return THUNDER_WOLF_TEXTURE;
    }
}
