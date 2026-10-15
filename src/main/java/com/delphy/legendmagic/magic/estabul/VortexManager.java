package com.delphy.legendmagic.magic.estabul;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 宙を覆う精霊の力（青い渦 / Vortex of Spirits）の持続処理を管理するサーバーマネージャー。
 * 指定座標に約8秒間持続する青い吸引渦を展開し、敵や飛来する矢を中心へ引き寄せ、継続スライスダメージを与える。
 */
public class VortexManager {

    private static final List<VortexInstance> ACTIVE_VORTICES = new ArrayList<>();

    public static void spawnVortex(ServerLevel level, Vec3 center, LivingEntity caster, float damage, int durationTicks) {
        ACTIVE_VORTICES.add(new VortexInstance(level, center, caster, damage, durationTicks));
    }

    public static void tick(ServerLevel level) {
        if (ACTIVE_VORTICES.isEmpty()) return;

        Iterator<VortexInstance> iterator = ACTIVE_VORTICES.iterator();
        while (iterator.hasNext()) {
            VortexInstance vortex = iterator.next();
            if (vortex.level == level) {
                boolean finished = vortex.tick();
                if (finished) {
                    iterator.remove();
                }
            }
        }
    }

    public static class VortexInstance {
        private final ServerLevel level;
        private final Vec3 center;
        private final LivingEntity caster;
        private final float damage;
        private int remainingTicks;
        private final int maxTicks;
        private double rotationAngle = 0.0D;

        public VortexInstance(ServerLevel level, Vec3 center, LivingEntity caster, float damage, int durationTicks) {
            this.level = level;
            this.center = center;
            this.caster = caster;
            this.damage = damage;
            this.remainingTicks = durationTicks;
            this.maxTicks = durationTicks;

            // 展開時の咆哮音
            level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.8F, 0.7F);
        }

        public boolean tick() {
            remainingTicks--;
            rotationAngle += 0.35D; // 回転速度

            double radius = 10.0D;
            AABB area = new AABB(center.x - radius, center.y - 4.0D, center.z - radius,
                    center.x + radius, center.y + 6.0D, center.z + radius);

            // 1. 周囲の敵Mobを中心へ強力にブラックホール吸引
            List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != caster && e.isAlive() && (caster == null || !e.isAlliedTo(caster)));

            for (LivingEntity enemy : enemies) {
                Vec3 toCenter = center.subtract(enemy.position());
                double dist = toCenter.length();
                if (dist > 0.5D) {
                    Vec3 pullDir = toCenter.normalize().scale(Math.min(0.35D, 0.1D + (1.0D / (dist + 1.0D))));
                    enemy.setDeltaMovement(enemy.getDeltaMovement().scale(0.5D).add(pullDir.x, 0.05D, pullDir.z));
                    enemy.hurtMarked = true;
                }

                // 10 ticks (0.5秒) ごとにスライス継続ダメージ
                if (remainingTicks % 10 == 0) {
                    if (caster != null) {
                        enemy.hurt(level.damageSources().indirectMagic(caster, caster), damage);
                    } else {
                        enemy.hurt(level.damageSources().magic(), damage);
                    }
                }
            }

            // 2. 飛来する矢・投擲物の吸引と中央での消滅 (飛び道具遮断)
            List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, area);
            for (Projectile proj : projectiles) {
                Vec3 toCenter = center.subtract(proj.position());
                double dist = toCenter.length();
                if (dist < 2.0D) {
                    // 中心に到達した飛び道具を消滅
                    level.sendParticles(ParticleTypes.SMOKE, proj.getX(), proj.getY(), proj.getZ(), 4, 0.1, 0.1, 0.1, 0.01);
                    proj.discard();
                } else {
                    Vec3 pullDir = toCenter.normalize().scale(0.4D);
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(0.6D).add(pullDir));
                }
            }

            // 3. 青い渦の螺旋パーティクル描画
            renderVortexParticles();

            // 4. 定期的な環境音
            if (remainingTicks % 20 == 0) {
                level.playSound(null, center.x, center.y, center.z,
                        SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.8F, 1.3F);
                level.playSound(null, center.x, center.y, center.z,
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6F, 1.5F);
            }

            // 終了時の収束炸裂
            if (remainingTicks <= 0) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 1.0D, center.z,
                        40, 1.5D, 1.0D, 1.5D, 0.1D);
                level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 1.0D, center.z,
                        2, 0.5D, 0.5D, 0.5D, 0.0D);
                level.playSound(null, center.x, center.y, center.z,
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.2F);
                return true;
            }

            return false;
        }

        private void renderVortexParticles() {
            // 4本の腕を持つ螺旋の渦
            int arms = 4;
            int pointsPerArm = 12;
            double maxR = 9.0D;

            for (int a = 0; a < arms; a++) {
                double armOffset = (Math.PI * 2.0D / arms) * a + rotationAngle;
                for (int p = 0; p < pointsPerArm; p++) {
                    double progress = (double) p / pointsPerArm;
                    double r = maxR * progress;
                    // 対数螺旋
                    double theta = armOffset + progress * Math.PI * 2.5D;

                    double px = center.x + r * Math.cos(theta);
                    double pz = center.z + r * Math.sin(theta);
                    // 渦の中心に向かって少しすり鉢状に窪む
                    double py = center.y + (progress * 1.5D);

                    // 青いソウルファイアとスカルクソウル粒子
                    if (p % 2 == 0) {
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 1, 0, 0.02D, 0, 0.01D);
                    } else {
                        level.sendParticles(ParticleTypes.SCULK_SOUL, px, py, pz, 1, 0, 0.04D, 0, 0.02D);
                    }
                }
            }

            // 中心の蒼い光柱
            level.sendParticles(ParticleTypes.GLOW, center.x, center.y + 1.0D, center.z, 3, 0.3D, 0.8D, 0.3D, 0.02D);
        }
    }
}
