package com.delphy.legendmagic.item;

import com.delphy.legendmagic.magic.SpellDataSyncHandler;
import com.delphy.legendmagic.magic.roland.IzuchiDeath;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * ボス「運命の破壊者」撃破ドロップ品「破壊者の魔導核」
 * 右クリックで使用することで、ローランド上位魔法「稲死光<いづち>」を恒久解禁・魔導書に記録する。
 */
public class CoreOfFateItem extends Item {

    public CoreOfFateItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            com.delphy.legendmagic.api.Spell izuchiDeath = new IzuchiDeath();
            boolean alreadyLearned = com.delphy.legendmagic.magic.SpellManager.hasLearned(serverPlayer, izuchiDeath);

            if (!alreadyLearned) {
                com.delphy.legendmagic.magic.SpellManager.learnSpell(serverPlayer, izuchiDeath);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 0.8F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 2.0F);

                player.displayClientMessage(
                        Component.literal("破壊者の魔導核を解放した！ ローランド上位魔法『稲死光<いづち>』を習得した！")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                        false
                );

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResultHolder.consume(stack);
            } else {
                player.displayClientMessage(
                        Component.literal("すでに『稲死光<いづち>』の構成式を理解・記録している。")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("運命の破壊者が秘めていた無詠唱魔法の極意が凝縮された核。").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("使用することで、ローランド上位魔法『稲死光<いづち>』を習得する。").withStyle(ChatFormatting.GOLD));
    }
}
