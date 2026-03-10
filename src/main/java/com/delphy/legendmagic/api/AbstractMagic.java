package com.delphy.legendmagic.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface AbstractMagic {
    ResourceLocation getSpellId(); // 魔法を識別する固有ID
    String getName();
    String getChant();
    String getDescription();
    default int getCastTime() { return 40; } // デフォルトで2秒 (20tick = 1s)
    void execute(ServerPlayer player);
}