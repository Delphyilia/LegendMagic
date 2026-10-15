package com.delphy.legendmagic.block;

import com.delphy.legendmagic.entity.DestroyerOfFateEntity;
import com.delphy.legendmagic.entity.ModEntities;
import com.delphy.legendmagic.item.InnerKnifeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Cataclysm風の専用ボススポナー「運命の祭壇」
 * キーアイテム「内面のナイフ」を持って右クリックすることで、
 * いつでも何度でも「運命の破壊者」を召喚・再戦することができる。
 */
public class AltarOfFateBlock extends Block {

    public AltarOfFateBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof InnerKnifeItem) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                // 重複召喚チェック（周囲64m以内に生存しているか）
                AABB checkArea = new AABB(pos).inflate(64.0D);
                List<DestroyerOfFateEntity> bosses = serverLevel.getEntitiesOfClass(DestroyerOfFateEntity.class, checkArea, DestroyerOfFateEntity::isAlive);

                if (!bosses.isEmpty()) {
                    player.displayClientMessage(
                            Component.literal("§c既に『運命の破壊者』が存在しています！ 決着をつけてください。"),
                            true
                    );
                    return InteractionResult.CONSUME;
                }

                // ボス召喚
                DestroyerOfFateEntity boss = ModEntities.DESTROYER_OF_FATE.get().create(serverLevel);
                if (boss != null) {
                    BlockPos spawnPos = pos.above();
                    boss.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                    serverLevel.addFreshEntity(boss);

                    // 召喚演出: 落雷・轟音・衝撃波
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                    if (bolt != null) {
                        bolt.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                        bolt.setVisualOnly(true);
                        serverLevel.addFreshEntity(bolt);
                    }

                    serverLevel.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.8F);
                    serverLevel.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 3.0F, 1.0F);

                    serverLevel.sendParticles(ParticleTypes.FLASH, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                            5, 0.5D, 0.5D, 0.5D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                            60, 1.0D, 1.0D, 1.0D, 0.05D);
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                            40, 1.2D, 0.8D, 1.2D, 0.08D);

                    player.displayClientMessage(
                            Component.literal("§5§l運命の破壊者が現界した……！『我が運命を切り拓く刃を持ってきたか』")
                                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                            false
                    );

                    player.getCooldowns().addCooldown(stack.getItem(), 100);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("§7祭壇には刃を受け止める窪みがある。『内面のナイフ』が必要なようだ……"),
                        true
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}
