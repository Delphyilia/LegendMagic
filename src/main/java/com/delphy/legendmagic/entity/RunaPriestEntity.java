package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.runa.SukuinoHonoowo;
import com.delphy.legendmagic.magic.runa.SukuinoIkazuchiwo;
import com.delphy.legendmagic.magic.runa.SukuinoKazewo;
import com.delphy.legendmagic.util.EyeUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

/**
 * ルーナ神官（Runa Priest）
 * ルーナ神殿に暮らす敬虔な聖職者（友好・中立Mob）。
 * エメラルドをお布施として捧げると、感謝とともにルーナの生活支援魔法を眼前で実演してくれる。
 * プレイヤーは複写眼（アルファ・スティグマ）でその詠唱と幾何学陣を目視することで、安全にルーナ魔法を解析・習得できる。
 */
public class RunaPriestEntity extends PathfinderMob {

    private static final List<Spell> RUNA_SPELLS = List.of(
            new SukuinoKazewo(),
            new SukuinoHonoowo(),
            new SukuinoIkazuchiwo()
    );

    private int chantCooldown = 0;

    public RunaPriestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.chantCooldown > 0) {
            this.chantCooldown--;
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!this.level().isClientSide) {
            if (this.chantCooldown > 0) {
                player.displayClientMessage(Component.literal("§7【ルーナ神官】「魔力の回復に少し祈りの時間が必要です……少々お待ちください」"), true);
                return InteractionResult.SUCCESS;
            }

            if (held.is(Items.EMERALD)) {
                // お布施消費
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                this.chantCooldown = 140; // 7秒クールダウン

                // プレイヤーが未習得のルーナ魔法を優先選択
                Spell spellToCast = selectSpellForPlayer(player);

                // 感謝のセリフ
                player.sendSystemMessage(Component.literal("§a【ルーナ神官】§f「尊いお布施をありがとうございます。ルーナの慈悲深き御業をお見せいたしましょう」")
                        .withStyle(ChatFormatting.AQUA));

                // 詠唱発表
                spellToCast.announceChant(this);

                // 魔法発動
                spellToCast.execute(this);

                // 複写眼解析用イベント発火
                MinecraftForge.EVENT_BUS.post(new MagicCastEvent(this, spellToCast));

                // 演出（ハッピーパーティクル＆SE）
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + 1.2D, this.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.1D);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }

                if (!EyeUtil.hasAlphaStigma(player)) {
                    player.sendSystemMessage(Component.literal("§7（※複写眼<アルファ・スティグマ>を装備してこの実演を注視すれば、魔法式を解析・習得できます）"));
                }

                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.literal("§a【ルーナ神官】§f「旅の御方、神殿へのエメラルドをお布施いただければ、ルーナの救いの術式をお見せいたします」"));
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private Spell selectSpellForPlayer(Player player) {
        for (Spell spell : RUNA_SPELLS) {
            if (!SpellManager.hasLearned(player, spell)) {
                return spell;
            }
        }
        // 全て習得済みの場合はランダム
        return RUNA_SPELLS.get(this.random.nextInt(RUNA_SPELLS.size()));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 攻撃されたら悲鳴SE
        this.playSound(SoundEvents.VILLAGER_HURT, 1.0F, 1.0F);
        return super.hurt(source, amount);
    }
}
