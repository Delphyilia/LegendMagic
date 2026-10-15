package com.delphy.legendmagic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 忘却欠片（古代の遺物 / Relics）の基底クラス。
 * 通常のホットバー携行型アイテムであり、儀式祭壇によって段階的に強化（Tier 0〜3）できる。
 */
public abstract class RelicItem extends Item {

    public static final String TAG_RELIC_LEVEL = "RelicLevel";
    public static final int MAX_RELIC_LEVEL = 3;

    public RelicItem(Properties properties) {
        super(properties);
    }

    public static int getRelicLevel(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_RELIC_LEVEL)) {
            return stack.getTag().getInt(TAG_RELIC_LEVEL);
        }
        return 0;
    }

    public static void setRelicLevel(ItemStack stack, int level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_RELIC_LEVEL, Math.max(0, Math.min(level, MAX_RELIC_LEVEL)));
    }

    public static String getRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "+I";
            case 2 -> "+II";
            case 3 -> "+III";
            default -> "";
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);
        int level = getRelicLevel(stack);
        if (level > 0) {
            return Component.empty().append(base).append(" §6" + getRomanNumeral(level));
        }
        return base;
    }

    /**
     * 強化段階に応じたクールダウン時間（ticks）を取得
     */
    public int getCooldownTicks(int level) {
        // Level 0: 12000 (10分), Level 1: 9600 (8分), Level 2: 7200 (6分), Level 3: 4800 (4分)
        return Math.max(2400, 12000 - (level * 2400));
    }

    /**
     * 強化段階に応じた召喚数・効果規模を取得
     */
    public int getSummonCount(int level) {
        // Level 0: 2体, Level 1: 3体, Level 2: 4体, Level 3: 5体
        return 2 + level;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        int currentLevel = getRelicLevel(stack);
        int cooldownSec = getCooldownTicks(currentLevel) / 20;
        int summonCount = getSummonCount(currentLevel);

        tooltipComponents.add(Component.literal("§6【忘却欠片】 §e強化段階: " + (currentLevel == 0 ? "未強化" : getRomanNumeral(currentLevel))));
        tooltipComponents.add(Component.literal("§7召喚数: §b" + summonCount + " 体 §7/ 持続: §a5分"));
        tooltipComponents.add(Component.literal("§7再使用待機時間: §c" + (cooldownSec / 60) + "分" + (cooldownSec % 60 > 0 ? " " + (cooldownSec % 60) + "秒" : "")));

        if (currentLevel < MAX_RELIC_LEVEL) {
            tooltipComponents.add(Component.literal("§8※進化祭壇で「遺物の欠片」を捧げることで強化可能"));
        } else {
            tooltipComponents.add(Component.literal("§d★ 限界突破（最大強化）完了"));
        }
    }
}
