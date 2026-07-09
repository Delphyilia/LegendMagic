package com.delphy.legendmagic.entity.ai;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.entity.RolandSoldierEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class MagicAttackGoal extends Goal {
    private final RolandSoldierEntity mob;
    private final Spell spell;
    private int attackTimer = -1;
    private final int castTime;

    public MagicAttackGoal(RolandSoldierEntity mob, Spell spell) {
        this.mob = mob;
        this.spell = spell;
        this.castTime = spell.getCastTime();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && this.mob.distanceToSqr(target) < 400.0D; // 20ブロック以内
    }

    @Override
    public void start() {
        this.attackTimer = 0;
        // AIが攻撃を開始した瞬間に詠唱文を表示
        this.spell.announceChant(this.mob);
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.attackTimer++;

        // 詠唱中に腕を振るモーション（視覚的ヒント）
        if (this.attackTimer % 10 == 0) {
            this.mob.swing(this.mob.getUsedItemHand());
        }

        if (this.attackTimer >= this.castTime) {
            this.mob.performMagicAttack(this.spell);
            this.attackTimer = -40; // クールタイム（2秒）
        }
    }

    @Override
    public void stop() {
        this.attackTimer = -1;
    }
}