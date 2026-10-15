package com.delphy.legendmagic.client.renderer;

import com.delphy.legendmagic.block.entity.RelicAltarBlockEntity;
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

public class RelicAltarRenderer implements BlockEntityRenderer<RelicAltarBlockEntity> {

    public RelicAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RelicAltarBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack relic = entity.getStoredRelic();
        if (relic.isEmpty()) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(relic, entity.getLevel(), null, 0);

        poseStack.pushPose();

        float gameTime = entity.getLevel() != null ? entity.getLevel().getGameTime() + partialTick : 0.0F;
        float bob = Mth.sin(gameTime * 0.1F) * 0.06F;

        // 台座の上（高さ1.15）に浮遊
        poseStack.translate(0.5D, 1.15D + bob, 0.5D);

        // ゆっくりとY軸回転
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime * 3.0F));
        poseStack.scale(0.65F, 0.65F, 0.65F);

        itemRenderer.render(relic, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, packedOverlay, bakedModel);

        poseStack.popPose();
    }
}
