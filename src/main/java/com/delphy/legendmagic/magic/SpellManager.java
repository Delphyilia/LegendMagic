package com.delphy.legendmagic.magic;

import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.network.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * プレイヤーの魔法データ（習得・装備・選択）を管理するユーティリティクラス。
 * データはプレイヤーのPersistentData (NBT) に保存される。
 */
public class SpellManager {
    // NBTに保存する際のキー名
    public static final String TAG_EQUIPPED = "LegendMagic_Equipped";
    public static final String TAG_LEARNED = "LegendMagic_Learned";
    public static final String TAG_SELECTED_INDEX = "LegendMagic_SelectedIdx";

    /** 装備可能な魔法スロットの最大数 */
    public static final int MAX_SPELL_SLOTS = 5;

    /**
     * 現在選択中の魔法を取得（プレイヤーごとのNBTから）
     */
    public static Spell getSelectedSpell(Player player) {
        int index = getSelectedIndex(player);
        List<Spell> equipped = getEquippedSpells(player);

        if (equipped.isEmpty() || index < 0 || index >= equipped.size()) {
            return null;
        }
        return equipped.get(index);
    }

    /**
     * 選択スロットを次に進める
     */
    public static void cycleSelectedSpell(Player player) {
        List<Spell> equipped = getEquippedSpells(player);
        if (equipped.isEmpty()) return;

        int nextIndex = (getSelectedIndex(player) + 1) % equipped.size();
        player.getPersistentData().putInt(TAG_SELECTED_INDEX, nextIndex);
    }

    /**
     * 魔法を習得する（複写眼で見た時に呼び出す）
     */
    public static void learnSpell(ServerPlayer player, Spell spell) {
        CompoundTag data = player.getPersistentData();
        ListTag learnedList = data.getList(TAG_LEARNED, Tag.TAG_STRING);
        String spellId = spell.getSpellId().toString();

        if (!hasLearned(player, spell)) {
            learnedList.add(StringTag.valueOf(spellId));
            data.put(TAG_LEARNED, learnedList);

            // クライアントへ同期
            CompoundTag syncData = new CompoundTag();
            syncData.put(TAG_LEARNED, learnedList);
            ModNetwork.sendToClient(new ModNetwork.SyncSpellDataPacket(syncData), player);
        }
    }

    /**
     * 習得済みかチェック
     */
    public static boolean hasLearned(Player player, Spell spell) {
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
    public static void setSpellAt(Player player, int slot, Spell spell) {
        if (slot < 0 || slot >= MAX_SPELL_SLOTS) return;

        CompoundTag data = player.getPersistentData();
        ListTag equippedList = data.getList(TAG_EQUIPPED, Tag.TAG_STRING);

        while (equippedList.size() < MAX_SPELL_SLOTS) {
            equippedList.add(StringTag.valueOf("none"));
        }

        equippedList.set(slot, StringTag.valueOf(spell.getSpellId().toString()));
        data.put(TAG_EQUIPPED, equippedList);

        // サーバー側で実行されている場合、クライアントへ同期を送る
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag syncData = new CompoundTag();
            syncData.put(TAG_EQUIPPED, equippedList);
            ModNetwork.sendToClient(new ModNetwork.SyncSpellDataPacket(syncData), serverPlayer);
        }
    }

    /**
     * 装備中の魔法リストを取得
     */
    public static List<Spell> getEquippedSpells(Player player) {
        List<Spell> spells = new ArrayList<>();
        ListTag list = player.getPersistentData().getList(TAG_EQUIPPED, Tag.TAG_STRING);

        for (int i = 0; i < list.size(); i++) {
            String idStr = list.getString(i);
            if (idStr.equals("none")) continue;

            ResourceLocation id = new ResourceLocation(idStr);
            Spell spell = SpellRegistry.REGISTRY.get().getValue(id);
            if (spell != null) spells.add(spell);
        }
        return spells;
    }

    /**
     * 現在選択中のスロットインデックスを取得
     */
    public static int getSelectedIndex(Player player) {
        return player.getPersistentData().getInt(TAG_SELECTED_INDEX);
    }
}