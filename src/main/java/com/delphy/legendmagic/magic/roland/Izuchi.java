package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.util.EyeUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class Izuchi implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "izuchi");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "稲光";
    }

    @Override
    public String getChant() {
        return "求めるは雷鳴>>>稲光<いづち>";
    }

    @Override
    public String getDescription() {
        return "ローランド流の超速雷撃。視線先の単体に青い電光を直撃させ、極大ダメージを与える。";
    }

    @Override
    public int getCastTime() {
        return 40; // 2.0秒 (40 ticks)
    }

    @Override
    public int getManaCost() {
        return 25;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    /**
     * いづち専用の詠唱中エフェクト: 周囲に演出用の雷光と放電スパークを漂わせる
     */
    @Override
    public void onCastingTick(Player player) {
        if (player.level().random.nextFloat() < 0.25F) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 6.0D;
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 6.0D;

            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
            if (bolt != null) {
                bolt.moveTo(x, player.getY(), z);
                bolt.setVisualOnly(true);
                player.level().addFreshEntity(bolt);
            }
        }
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        double range = 35.0D;

        float damage = calculateDamage(caster);

        // ターゲットの特定 (レイキャスト)
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                caster, start, end,
                caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.2D),
                (e) -> !e.isSpectator() && e.isPickable(),
                range * range
        );

        Vec3 actualEnd = (entityHit != null) ? entityHit.getLocation() : end;

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            // 魔法属性ダメージ
            livingTarget.hurt(caster.damageSources().magic(), damage);

            // ターゲット位置に演出用の雷を落とす
            LightningBolt visualBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (visualBolt != null) {
                visualBolt.moveTo(livingTarget.position());
                visualBolt.setVisualOnly(true);
                level.addFreshEntity(visualBolt);
            }

            // ヒット時の放電・火花パーティクル
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() / 2.0D, livingTarget.getZ(),
                    30, 0.4D, 0.6D, 0.4D, 0.15D);

            level.playSound(null, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.2F, 1.4F);
        }

        // 鋭利な電光ビームの軌跡を描画
        spawnElectricTrail(level, start, actualEnd);

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.8F);
    }

    private float calculateDamage(LivingEntity caster) {
        float baseDamage = 16.0F; // 基礎ダメージ
        float multiplier = 1.0F;

        if (caster instanceof Player player) {
            if (EyeUtil.hasAlphaStigma(player)) {
                multiplier = 2.0F; // 複写眼持ちなら2倍 (32ダメージ)
            }
        }

        return baseDamage * multiplier;
    }

    private void spawnElectricTrail(ServerLevel level, Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 3);

        for (int i = 0; i < particleCount; i++) {
            double ratio = (double) i / particleCount;
            double x = start.x + (end.x - start.x) * ratio;
            double y = start.y + (end.y - start.y) * ratio;
            double z = start.z + (end.z - start.z) * ratio;

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}