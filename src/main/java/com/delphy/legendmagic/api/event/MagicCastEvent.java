package com.delphy.legendmagic.api.event;

import com.delphy.legendmagic.api.AbstractMagic;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 誰かが魔法を放った時に発行されるイベント
 */
public class MagicCastEvent extends Event {
    private final LivingEntity caster;
    private final AbstractMagic magic;

    public MagicCastEvent(LivingEntity caster, AbstractMagic magic) {
        this.caster = caster;
        this.magic = magic;
    }

    public LivingEntity getCaster() { return caster; }
    public AbstractMagic getMagic() { return magic; }
}