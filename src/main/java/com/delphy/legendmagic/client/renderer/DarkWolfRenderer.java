package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.entity.DarkWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;

public class DarkWolfRenderer extends WolfRenderer {
    private static final ResourceLocation DARK_WOLF_TEXTURE = new ResourceLocation(LegendMagic.MODID, "textures/entity/dark_wolf/dark_wolf.png");

    public DarkWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.Wolf wolf) {
        return DARK_WOLF_TEXTURE;
    }
}
