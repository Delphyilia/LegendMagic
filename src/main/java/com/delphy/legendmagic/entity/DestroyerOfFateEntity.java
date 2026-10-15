package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.item.LegendMagicItems;
import com.delphy.legendmagic.magic.roland.Chigashira;
import com.delphy.legendmagic.magic.roland.IzuchiDeath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.EnumSet;

/**
 * 最高峰エンドコンテンツボス「運命の破壊者（The Destroyer of Fate）」
 * 魔法の天才であり、全魔法を完全無詠唱・ノータイムで連発する。
 * 残影テレポートによる神速の回避機動と、殲滅眼貫通の「倒地」、
 * そして頭上横雷「稲死光」の乱射でプレイヤーを圧倒する。
 */
public class DestroyerOfFateEntity extends Monster {

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(
            Component.literal("§5§l運命の破壊者 §7- The Destroyer of Fate -"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    )).setDarkenScreen(true);

    private static final Spell SPELL_IZUCHI_DEATH = new IzuchiDeath();
    private static final Spell SPELL_CHIGASHIRA = new Chigashira();

    private int teleportCooldown = 0;

    public DestroyerOfFateEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 行動AI: 完全無詠唱 稲死光 乱射 (優先度1)
        this.goalSelector.addGoal(1, new DestroyerMagicGoal(this));

        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // ボスは自然消滅しない
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }

        // 常時周囲に不気味な暗黒魔力パーティクル
        if (this.level() instanceof ServerLevel sLevel && this.tickCount % 4 == 0) {
            sLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    3, 0.4D, 0.8D, 0.4D, 0.02D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 攻撃を受けた際に確率で残影テレポート発動（神速の身ごなし）
        if (!this.level().isClientSide && this.teleportCooldown <= 0 && this.random.nextFloat() < 0.35F) {
            teleportRandomly();
            this.teleportCooldown = 60; // 3秒クールダウン
        }
        return super.hurt(source, amount);
    }

    /**
     * 残影テレポート
     */
    public void teleportRandomly() {
        if (!(this.level() instanceof ServerLevel sLevel)) return;

        double currentX = this.getX();
        double currentY = this.getY();
        double currentZ = this.getZ();

        LivingEntity target = this.getTarget();
        double targetX = (target != null) ? target.getX() : currentX;
        double targetZ = (target != null) ? target.getZ() : currentZ;

        // ターゲットの背後や横の8〜12mへ転移
        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double dist = 8.0D + this.random.nextDouble() * 6.0D;
        double newX = targetX + Math.cos(angle) * dist;
        double newZ = targetZ + Math.sin(angle) * dist;
        double newY = currentY;

        // 元の位置に残影パーティクル＆音
        sLevel.sendParticles(ParticleTypes.PORTAL, currentX, currentY + 1.0D, currentZ, 40, 0.4D, 0.8D, 0.4D, 0.1D);
        sLevel.playSound(null, currentX, currentY, currentZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 1.2F);

        this.teleportTo(newX, newY, newZ);

        // 出現位置にパーティクル
        sLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, newX, newY + 1.0D, newZ, 40, 0.4D, 0.8D, 0.4D, 0.1D);
        sLevel.playSound(null, newX, newY, newZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 0.8F);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);
        // 確定ドロップ: 破壊者の魔導核
        this.spawnAtLocation(LegendMagicItems.CORE_OF_FATE.get());
    }

    /**
     * 運命の破壊者専用AI: 完全無詠唱での魔法連射＆倒地による奇襲
     */
    static class DestroyerMagicGoal extends Goal {
        private final DestroyerOfFateEntity boss;
        private int izuchiCooldown = 40;
        private int chigashiraCooldown = 120;

        public DestroyerMagicGoal(DestroyerOfFateEntity boss) {
            this.boss = boss;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.boss.getTarget() != null && this.boss.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.boss.getTarget();
            if (target == null) return;

            this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.boss.distanceToSqr(target);

            // 適正交戦距離（12〜24m）を保つ
            if (distSq > 24 * 24) {
                this.boss.getNavigation().moveTo(target, 1.2D);
            } else if (distSq < 8 * 8) {
                // 近すぎたら距離を取る
                Vec3 away = this.boss.position().subtract(target.position()).normalize().scale(8);
                this.boss.getNavigation().moveTo(this.boss.getX() + away.x, this.boss.getY(), this.boss.getZ() + away.z, 1.2D);
            } else {
                this.boss.getNavigation().stop();
            }

            if (this.izuchiCooldown > 0) this.izuchiCooldown--;
            if (this.chigashiraCooldown > 0) this.chigashiraCooldown--;

            // 1. 倒地（ちがしら）の発動判定（約6秒ごと）: 地中物理杭、殲滅眼貫通！
            if (this.chigashiraCooldown <= 0 && distSq < 32 * 32) {
                this.boss.castSpell(SPELL_CHIGASHIRA);
                this.chigashiraCooldown = 120 + this.boss.random.nextInt(40);
                this.izuchiCooldown = Math.max(this.izuchiCooldown, 25); // 倒地直後の隙
                return;
            }

            // 2. 稲死光（いづち）の発動判定（約2秒ごと）: 完全無詠唱の頭上横雷スナイプ！
            if (this.izuchiCooldown <= 0 && distSq < 48 * 48) {
                this.boss.castSpell(SPELL_IZUCHI_DEATH);
                this.izuchiCooldown = 45 + this.boss.random.nextInt(25);
            }
        }
    }

    public void castSpell(Spell spell) {
        if (!this.level().isClientSide) {
            spell.execute(this);
            // 複写眼の視界コピー判定用イベント
            MinecraftForge.EVENT_BUS.post(new MagicCastEvent(this, spell));
        }
    }
}
