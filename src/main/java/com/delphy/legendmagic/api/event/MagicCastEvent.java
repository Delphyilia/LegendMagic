package com.delphy.legendmagic.api.event;

import com.delphy.legendmagic.api.Spell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 誰かが魔法を放った時に発行されるイベント
 */
public class MagicCastEvent extends Event {
    private final LivingEntity caster;
    private final Spell magic;

    public MagicCastEvent(LivingEntity caster, Spell magic) {
        this.caster = caster;
        this.magic = magic;
    }

    public LivingEntity getCaster() { return caster; }
    public Spell getMagic() { return magic; }
}