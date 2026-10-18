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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 求めるは侵入>>>蝕走<しら> (Shira)
 * ローランドで使用される反魔法（アンチ・マジック）。
 * 魔方陣の中央より黒き煙の侵蝕弾を放ち、射線上の敵の魔法飛翔体を消去し、
 * 着弾した敵魔導士の有利バフ（自己強化等）を強制消去・解除して魔力に中ダメージを与える。
 */
public class Shira implements Spell {

    public static final ResourceLocation ID = new ResourceLocation(LegendMagic.MODID, "shira");

    @Override
    public ResourceLocation getSpellId() {
        return ID;
    }

    @Override
    public String getName() {
        return "蝕走<しら>";
    }

    @Override
    public String getChant() {
        return "求めるは侵入>>>蝕走<しら>";
    }

    @Override
    public String getDescription() {
        return "黒き煙の侵蝕弾を放つ反魔法（アンチマジック）。敵の魔法弾を消滅させ、敵の強化効果を強制解除する。";
    }

    @Override
    public int getCastTime() {
        return 20; // 1.0秒（素早いカウンター性能）
    }

    @Override
    public int getManaCost() {
        return 20;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 start = caster.getEyePosition();
        Vec3 look = com.delphy.legendmagic.util.MagicUtil.getSpellAimDirection(caster, start);
        double range = 24.0D;

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.8F, 1.6F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 1.8F);

        // 1. 射線上の敵飛び道具（魔法弾・矢など）を侵蝕消去
        AABB beamBox = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D);
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, beamBox);
        for (Projectile proj : projectiles) {
            Vec3 toProj = proj.position().subtract(start);
            if (toProj.normalize().dot(look) > 0.8D) {
                level.sendParticles(ParticleTypes.SQUID_INK, proj.getX(), proj.getY(), proj.getZ(),
                        15, 0.2, 0.2, 0.2, 0.05);
                level.playSound(null, proj.getX(), proj.getY(), proj.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2F, 1.5F);
                proj.discard(); // 魔法弾を打ち消し消去
            }
        }

        // 2. 黒煙の侵蝕パーティクル軌跡
        int steps = (int) (range * 3);
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = start.add(look.scale(progress * range));
            level.sendParticles(ParticleTypes.SQUID_INK, pos.x, pos.y, pos.z, 3, 0.1, 0.1, 0.1, 0.02);
            level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.01);
        }

        // 3. 標的のバフ強制解除 ＆ 魔力ダメージ
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, beamBox,
                e -> e != caster && e.isAlive());

        boolean hasEye = (caster instanceof Player p && EyeUtil.hasAlphaStigma(p));
        float damage = hasEye ? 20.0F : 10.0F;

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(start);
            if (toTarget.normalize().dot(look) < 0.8D) continue;

            // バフの強制解除（アンチマジック効果）
            target.removeAllEffects();

            // 魔法消滅ダメージ
            target.hurt(caster.damageSources().magic(), damage);

            level.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0D, target.getZ(),
                    30, 0.4D, 0.6D, 0.4D, 0.1D);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.2F, 1.8F);
        }

        announceChant(caster);
    }
}
