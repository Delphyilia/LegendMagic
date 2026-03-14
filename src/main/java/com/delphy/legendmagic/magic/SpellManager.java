package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.AbstractMagic;
import com.delphy.legendmagic.network.ModNetwork;
import com.delphy.legendmagic.network.SyncLearnedSpellsPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SpellManager {
    // NBTに保存する際のキー名
    // 装備済みの魔法
    public static final String TAG_EQUIPPED = "LegendMagic_Equipped";

    // 学習済みの魔法
    public static final String TAG_LEARNED = "LegendMagic_Learned";

    // 現在選んでいるスロット
    public static final String TAG_SELECTED_INDEX = "LegendMagic_SelectedIdx";

    /**
     * 現在選択中の魔法を取得（プレイヤーごとのNBTから）
     */
    public static AbstractMagic getCurrent(Player player) {
        int index = getSelectedIndex(player);
        List<AbstractMagic> equipped = getEquippedSpells(player);

        if (equipped.isEmpty() || index < 0 || index >= equipped.size()) {
            return null;
        }
        return equipped.get(index);
    }

    /**
     * 選択スロットを次に進める
     */
    public static void next(Player player) {
        List<AbstractMagic> equipped = getEquippedSpells(player);
        if (equipped.isEmpty()) return;

        int nextIndex = (getSelectedIndex(player) + 1) % equipped.size();
        player.getPersistentData().putInt(TAG_SELECTED_INDEX, nextIndex);
    }

    /**
     * 魔法を習得する（複写眼で見た時に呼び出す）
     */
    public static void learnSpell(ServerPlayer player, AbstractMagic spell) {
        CompoundTag data = player.getPersistentData();
        ListTag learnedList = data.getList(TAG_LEARNED, Tag.TAG_STRING);
        String spellId = spell.getSpellId().toString();

        if (!hasLearned(player, spell)) {
            learnedList.add(StringTag.valueOf(spellId));
            data.put(TAG_LEARNED, learnedList);

            // クライアントへ同期。新しいパケットクラスを作成済みである前提です。
            CompoundTag syncData = new CompoundTag();
            syncData.put(TAG_LEARNED, learnedList);
            ModNetwork.sendToClient(new SyncLearnedSpellsPacket(syncData), player);
        }
    }

    /**
     * 習得済みかチェック
     */
    public static boolean hasLearned(Player player, AbstractMagic spell) {
        ListTag learnedList = player.getPersistentData().getList(TAG_LEARNED, Tag.TAG_STRING);
        String targetId = spell.getSpellId().toString();

        for (int i = 0; i < learnedList.size(); i++) {
            if (learnedList.getString(i).equals(targetId)) return true;
        }
        return false;
    }

    /**
     * スロットに魔法をセットする
     */
    public static void setSpellAt(Player player, int slot, AbstractMagic spell) {
        if (slot < 0 || slot >= 5) return;

        CompoundTag data = player.getPersistentData();
        ListTag equippedList = data.getList(TAG_EQUIPPED, Tag.TAG_STRING);

        while (equippedList.size() < 5) {
            equippedList.add(StringTag.valueOf("none"));
        }

        equippedList.set(slot, StringTag.valueOf(spell.getSpellId().toString()));
        data.put(TAG_EQUIPPED, equippedList);

        // ⭐修正: サーバー側で実行されている場合、クライアントへ同期を送る
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag syncData = new CompoundTag();
            syncData.put(TAG_EQUIPPED, equippedList);
            ModNetwork.sendToClient(new SyncLearnedSpellsPacket(syncData), serverPlayer);
        }
    }

    public static List<AbstractMagic> getEquippedSpells(Player player) {
        List<AbstractMagic> spells = new ArrayList<>();
        ListTag list = player.getPersistentData().getList(TAG_EQUIPPED, Tag.TAG_STRING);

        for (int i = 0; i < list.size(); i++) {
            String idStr = list.getString(i);
            // ⭐修正: "none" の場合は飛ばす
            if (idStr.equals("none")) continue;

            ResourceLocation id = new ResourceLocation(idStr);
            AbstractMagic magic = SpellRegistry.REGISTRY.get().getValue(id);
            if (magic != null) spells.add(magic);
        }
        return spells;
    }

    public static int getSelectedIndex(Player player) {
        return player.getPersistentData().getInt(TAG_SELECTED_INDEX);
    }
}