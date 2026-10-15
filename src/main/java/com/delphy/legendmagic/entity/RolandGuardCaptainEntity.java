package com.delphy.legendmagic.entity;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.api.event.MagicCastEvent;
import com.delphy.legendmagic.entity.ai.MagicAttackGoal;
import com.delphy.legendmagic.item.LegendMagicItems;
import com.delphy.legendmagic.magic.roland.Izuchi;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

/**
 * ローランド王国の最深部を守護する中ボス「ローランド近衛隊長」
 * 熟練の近接剣術と高速雷撃「稲光」を使いこなす強敵。
 * 討伐することで、魔法研究所への鍵「内面のナイフ」を確定ドロップする。
 */
import com.delphy.legendmagic.entity.ai.MagicCaster;

public class RolandGuardCaptainEntity extends Monster implements MagicCaster {

    private static final Spell SPELL = new Izuchi();

    public RolandGuardCaptainEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 優先度1: 魔法攻撃（ターゲットが射程内にいる時）
        this.goalSelector.addGoal(1, new MagicAttackGoal(this, SPELL));
        // 優先度2: 近接攻撃
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));

        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);
        // 確定ドロップ: キーアイテム「内面のナイフ」
        this.spawnAtLocation(LegendMagicItems.INNER_KNIFE.get());
    }

    public void performMagicAttack(Spell spell) {
        if (!this.level().isClientSide) {
            spell.execute(this);
            // 複写眼の視界コピー判定用イベント
            MinecraftForge.EVENT_BUS.post(new MagicCastEvent(this, spell));
        }
    }
}
