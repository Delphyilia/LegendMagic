package com.delphy.legendmagic.magic.roland;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.AbstractMagic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class Izuchi implements AbstractMagic {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "izuchi");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() { return "ライトニング"; }

    @Override
    public String getChant() { return "求めるは雷鳴>>>稲光<いづち>"; }

    @Override
    public String getDescription() { return "視線の先の敵に直接ダメージを与え、青い火花を散らす。"; }

    @Override
    public void execute(LivingEntity caster) {
        ServerLevel level = (ServerLevel) caster.level();
        double range = 30.0;

        // 独自ダメージ計算
        float damage = calculateDamage(caster);

        // ターゲットの特定
        Vec3 start = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                caster, start, end,
                caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D),
                (e) -> !e.isSpectator() && e.isPickable(),
                range * range
        );

        Vec3 actualEnd = (entityHit != null) ? entityHit.getLocation() : end;

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            // 1. 魔法属性ダメージの適用
            livingTarget.hurt(caster.damageSources().magic(), damage);

            // 2. ターゲットに「見た目だけの雷」を落とす
            LightningBolt visualBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (visualBolt != null) {
                visualBolt.moveTo(livingTarget.position());
                visualBolt.setVisualOnly(true); // ダメージ・炎上・破壊を無効化
                level.addFreshEntity(visualBolt);
            }

            // 3. ヒット時のパーティクル
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    livingTarget.getX(), livingTarget.getY() + 1, livingTarget.getZ(),
                    20, 0.2, 0.5, 0.2, 0.1);
        }

        // 軌跡の表示
        spawnElectricTrail(level, start, actualEnd);
    }

    /**
     * ダメージ計算メソッド
     */
    private float calculateDamage(LivingEntity caster) {
        float baseDamage = 10.0F;
        float multiplier = 1.0F;

        // ⭐修正：casterがプレイヤーの場合のみ、複写眼のチェックを行う
        if (caster instanceof net.minecraft.world.entity.player.Player player) {
            if (com.delphy.legendmagic.util.EyeUtil.hasAlphaStigma(player)) {
                multiplier = 2.0F; // プレイヤーかつ複写眼持ちなら2倍
            }
        } else {
            // モブが撃つ場合の補正が必要ならここに書く（例：モブの攻撃力に依存させるなど）
            // 今はそのまま multiplier = 1.0F
        }

        return baseDamage * multiplier;
    }

    private void spawnElectricTrail(ServerLevel level, Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 2);

        for (int i = 0; i < particleCount; i++) {
            double ratio = (double) i / particleCount;
            double x = start.x + (end.x - start.x) * ratio;
            double y = start.y + (end.y - start.y) * ratio;
            double z = start.z + (end.z - start.z) * ratio;

            // ソウルファイアのパーティクルを軌跡として使用
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}