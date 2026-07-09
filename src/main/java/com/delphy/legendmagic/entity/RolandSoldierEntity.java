package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.entity.ai.MagicAttackGoal;
import com.delphy.legendmagic.magic.roland.Izuchi;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

public class RolandSoldierEntity extends Monster {

    // この兵士が使う魔法
    private static final Spell SPELL = new Izuchi();

    public RolandSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        // 優先度1: 魔法攻撃（ターゲットが射程内にいる時）
        this.goalSelector.addGoal(1, new MagicAttackGoal(this, SPELL));

        // 優先度2: 近接攻撃（魔法が使えない時の予備）
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));

        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    /**
     * 魔法の発動実行
     */
    public void performMagicAttack(Spell spell) {
        if (!this.level().isClientSide) {
            spell.execute(this);

            // イベント発火（学習用）
            MinecraftForge.EVENT_BUS.post(new MagicCastEvent(this, spell));
        }
    }
}