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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

/**
 * 求めるは焼原>>>紅蓮<くれない> (Kurenai)
 * ローランド帝国が誇る高火力爆撃魔法。
 * 詠唱完了時、魔方陣から灼熱の火球を5発連続射出（ガトリング爆撃）し、敵集団を焼き尽くす。
 */
public class Kurenai implements Spell {

    public static final ResourceLocation ID = new ResourceLocation(LegendMagic.MODID, "kurenai");

    @Override
    public ResourceLocation getSpellId() {
        return ID;
    }

    @Override
    public String getName() {
        return "紅蓮<くれない>";
    }

    @Override
    public String getChant() {
        return "求めるは焼原>>>紅蓮<くれない>";
    }

    @Override
    public String getDescription() {
        return "魔方陣より灼熱の火球を5発連続射出する圧倒的殲滅魔法。直撃時に爆発と炎上を引き起こす。";
    }

    @Override
    public int getCastTime() {
        return 40; // 2.0秒
    }

    @Override
    public int getManaCost() {
        return 35;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.ROLAND;
    }

    @Override
    public void onCastingTick(Player player) {
        // 詠唱中に術者の周囲に火炎パーティクルが螺旋状に集束
        if (player.level().isClientSide) {
            double angle = System.currentTimeMillis() / 80.0D;
            double r = 1.2D;
            player.level().addParticle(ParticleTypes.FLAME,
                    player.getX() + Math.cos(angle) * r,
                    player.getY() + 0.8D + Math.sin(angle) * 0.4D,
                    player.getZ() + Math.sin(angle) * r,
                    0, 0.05, 0);
        }
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 eyePos = caster.getEyePosition();
        Vec3 look = com.delphy.legendmagic.util.MagicUtil.getSpellAimDirection(caster, eyePos);

        // 複写眼持ちなら火球の威力・爆発力アップ
        boolean hasEye = (caster instanceof Player p && EyeUtil.hasAlphaStigma(p));
        float damage = hasEye ? 14.0F : 8.0F;

        // 5発の連続火球射出
        int shotCount = 5;
        for (int i = 0; i < shotCount; i++) {
            double spreadX = (level.random.nextDouble() - 0.5D) * 0.15D;
            double spreadY = (level.random.nextDouble() - 0.5D) * 0.15D;
            double spreadZ = (level.random.nextDouble() - 0.5D) * 0.15D;
            Vec3 dir = look.add(spreadX, spreadY, spreadZ).normalize();

            SmallFireball fireball = new SmallFireball(level, caster, dir.x, dir.y, dir.z);
            fireball.setPos(eyePos.x + dir.x * 0.8D, eyePos.y + dir.y * 0.8D, eyePos.z + dir.z * 0.8D);
            level.addFreshEntity(fireball);
        }

        // 炎上エフェクト＆迫力の爆音
        level.sendParticles(ParticleTypes.LAVA, eyePos.x + look.x, eyePos.y + look.y, eyePos.z + look.z,
                25, 0.5D, 0.5D, 0.5D, 0.2D);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 2.0F, 0.8F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0F, 1.2F);

        announceChant(caster);
    }
}
