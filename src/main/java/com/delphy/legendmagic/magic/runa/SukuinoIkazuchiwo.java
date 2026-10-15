package com.delphy.legendmagic.magic.runa;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.MagicSystem;
import com.delphy.legendmagic.api.Spell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SukuinoIkazuchiwo implements Spell {

    private static final ResourceLocation SPELL_ID = new ResourceLocation(LegendMagic.MODID, "sukuino_ikazuchiwo");

    @Override
    public ResourceLocation getSpellId() {
        return SPELL_ID;
    }

    @Override
    public String getName() {
        return "救いの雷";
    }

    @Override
    public String getChant() {
        return "天在する神に請う。憐れな我らに、救いの雷を";
    }

    @Override
    public String getDescription() {
        return "天から真実の天然雷を呼び寄せる。帯電クリーパーや村人の魔女化に利用可能。";
    }

    @Override
    public int getCastTime() {
        return 30; // 1.5秒 (30 ticks)
    }

    @Override
    public int getManaCost() {
        return 20;
    }

    @Override
    public MagicSystem getMagicSystem() {
        return MagicSystem.RUNA;
    }

    @Override
    public void execute(LivingEntity caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        double range = 25.0D;

        Vec3 start = caster.getEyePosition();
        Vec3 look;

        if (caster instanceof Mob mob && mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            look = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(start).normalize();
        } else {
            look = caster.getLookAngle();
        }

        Vec3 end = start.add(look.scale(range));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                caster, start, end,
                caster.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D),
                (e) -> !e.isSpectator() && e.isPickable(),
                range * range
        );

        Vec3 targetPos;
        if (entityHit != null) {
            targetPos = entityHit.getEntity().position();
        } else {
            HitResult blockHit = caster.pick(range, 0.0F, false);
            targetPos = blockHit.getLocation();
        }

        // 天然の落雷を召喚
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(targetPos);
            if (caster instanceof ServerPlayer serverPlayer) {
                bolt.setCause(serverPlayer);
            }
            level.addFreshEntity(bolt);
        }
    }
}