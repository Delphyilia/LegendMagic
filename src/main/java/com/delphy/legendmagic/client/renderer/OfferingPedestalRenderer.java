package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.block.entity.OfferingPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class OfferingPedestalRenderer implements BlockEntityRenderer<OfferingPedestalBlockEntity> {

    public OfferingPedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OfferingPedestalBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack item = entity.getStoredItem();
        if (item.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(item, entity.getLevel(), null, 0);

        poseStack.pushPose();

        float gameTime = entity.getLevel() != null ? entity.getLevel().getGameTime() + partialTick : 0.0F;
        float bob = Mth.sin(gameTime * 0.12F) * 0.04F;

        // 台座の上（高さ1.05）に浮遊
        poseStack.translate(0.5D, 1.05D + bob, 0.5D);

        // ゆっくりとY軸逆回転
        poseStack.mulPose(Axis.YP.rotationDegrees(-gameTime * 2.5F));
        poseStack.scale(0.55F, 0.55F, 0.55F);

        itemRenderer.render(item, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, packedOverlay, bakedModel);

        poseStack.popPose();
    }
}
