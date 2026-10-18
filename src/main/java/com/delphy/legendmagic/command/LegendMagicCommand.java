package com.delphy.legendmagic.command;

import com.delphy.legendmagic.LegendMagic;
import com.delphy.legendmagic.api.Spell;
import com.delphy.legendmagic.block.ModBlocks;
import com.delphy.legendmagic.item.InnerKnifeItem;
import com.delphy.legendmagic.item.LegendMagicItems;
import com.delphy.legendmagic.item.MenorisCompassItem;
import com.delphy.legendmagic.magic.ManaManager;
import com.delphy.legendmagic.magic.SpellManager;
import com.delphy.legendmagic.magic.SpellRegistry;
import com.delphy.legendmagic.network.ModNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * LegendMagic 開発・デバッグプレイ用統合コマンド体系 (/legendmagic および /lm)
 *
 * サブコマンド:
 * - /lm learn all: 上級魔法を含む全登録魔法を一括習得
 * - /lm reset spells: プレイヤーの魔法習得状態・刻印スロットを完全初期化
 * - /lm mana [amount]: マナを全回復または指定値に設定
 * - /lm eggs: 全Mob・ボスのスポーンエッグ一式（各64個）をインベントリに付与
 * - /lm kit test: 検証・デバッグ用装備・遺物・キーアイテム一式を付与
 * - /lm tp <destination>: 各国構造物またはディメンションへ直接テレポート
 */
public class LegendMagicCommand {

    private static final List<String> TP_DESTINATIONS = List.of(
            "roland", "estabul", "runa", "gastark", "lab", "menoris", "overworld"
    );

    private static final SuggestionProvider<CommandSourceStack> TP_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(TP_DESTINATIONS, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("legendmagic")
                .requires(source -> source.hasPermission(2)) // OP権限レベル2以上

                // 1. 全魔法習得 (/lm learn all)
                .then(Commands.literal("learn")
                        .then(Commands.literal("all")
                                .executes(ctx -> executeLearnAll(ctx.getSource()))
                        )
                )

                // 2. 魔法データ初期化 (/lm reset spells)
                .then(Commands.literal("reset")
                        .then(Commands.literal("spells")
                                .executes(ctx -> executeResetSpells(ctx.getSource()))
                        )
                )

                // 3. マナ操作 (/lm mana [amount])
                .then(Commands.literal("mana")
                        .executes(ctx -> executeMana(ctx.getSource(), -1.0F))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                .executes(ctx -> executeMana(ctx.getSource(), FloatArgumentType.getFloat(ctx, "amount")))
                        )
                )

                // 4. スポーンエッグ一括入手 (/lm eggs)
                .then(Commands.literal("eggs")
                        .executes(ctx -> executeGiveEggs(ctx.getSource()))
                )

                // 5. テストキット入手 (/lm kit test)
                .then(Commands.literal("kit")
                        .then(Commands.literal("test")
                                .executes(ctx -> executeGiveKit(ctx.getSource()))
                        )
                )

                // 6. 各国構造物・ディメンションへのテレポート (/lm tp <destination>)
                .then(Commands.literal("tp")
                        .then(Commands.argument("destination", StringArgumentType.word())
                                .suggests(TP_SUGGESTIONS)
                                .executes(ctx -> executeTeleport(ctx.getSource(), StringArgumentType.getString(ctx, "destination")))
                        )
                );

        LiteralCommandNode<CommandSourceStack> rootNode = dispatcher.register(builder);

        // 短縮エイリアス "/lm" の登録
        dispatcher.register(Commands.literal("lm")
                .requires(source -> source.hasPermission(2))
                .redirect(rootNode)
        );
    }

    /**
     * 上位魔法を含む全魔法を一括習得
     */
    private static int executeLearnAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("このコマンドはプレイヤーのみ実行可能です。"));
            return 0;
        }

        Collection<Spell> allSpells = SpellRegistry.REGISTRY.get().getValues();
        CompoundTag data = player.getPersistentData();
        ListTag learnedList = data.getList(SpellManager.TAG_LEARNED, Tag.TAG_STRING);

        int addedCount = 0;
        for (Spell spell : allSpells) {
            String spellId = spell.getSpellId().toString();
            boolean alreadyLearned = false;
            for (int i = 0; i < learnedList.size(); i++) {
                if (learnedList.getString(i).equals(spellId)) {
                    alreadyLearned = true;
                    break;
                }
            }
            if (!alreadyLearned) {
                learnedList.add(StringTag.valueOf(spellId));
                addedCount++;
            }
        }
        data.put(SpellManager.TAG_LEARNED, learnedList);

        // 装備スロットが空、または未設定なら先頭から自動セット
        ListTag equippedList = data.getList(SpellManager.TAG_EQUIPPED, Tag.TAG_STRING);
        if (equippedList.isEmpty()) {
            int slot = 0;
            for (Spell spell : allSpells) {
                if (slot >= SpellManager.MAX_SPELL_SLOTS) break;
                equippedList.add(StringTag.valueOf(spell.getSpellId().toString()));
                slot++;
            }
            while (equippedList.size() < SpellManager.MAX_SPELL_SLOTS) {
                equippedList.add(StringTag.valueOf("none"));
            }
            data.put(SpellManager.TAG_EQUIPPED, equippedList);
        }

        // クライアントへ同期
        CompoundTag syncData = new CompoundTag();
        syncData.put(SpellManager.TAG_LEARNED, learnedList);
        syncData.put(SpellManager.TAG_EQUIPPED, data.getList(SpellManager.TAG_EQUIPPED, Tag.TAG_STRING));
        ModNetwork.sendToClient(new ModNetwork.SyncSpellDataPacket(syncData), player);

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);

        final int total = allSpells.size();
        final int count = addedCount;
        source.sendSuccess(() -> Component.literal(String.format(
                "§6[LegendMagic] §a上級魔法を含む全魔法（登録 %d 種 / 新規 %d 種）を解禁・習得しました！",
                total, count)), true);

        return 1;
    }

    /**
     * 魔法習得状態および刻印スロットの初期化
     */
    private static int executeResetSpells(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("このコマンドはプレイヤーのみ実行可能です。"));
            return 0;
        }

        CompoundTag data = player.getPersistentData();
        data.remove(SpellManager.TAG_LEARNED);
        data.remove(SpellManager.TAG_EQUIPPED);
        data.remove(SpellManager.TAG_SELECTED_INDEX);

        // 空のデータをクライアントへ同期
        CompoundTag syncData = new CompoundTag();
        syncData.put(SpellManager.TAG_LEARNED, new ListTag());
        syncData.put(SpellManager.TAG_EQUIPPED, new ListTag());
        ModNetwork.sendToClient(new ModNetwork.SyncSpellDataPacket(syncData), player);

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.5F);

        source.sendSuccess(() -> Component.literal("§6[LegendMagic] §c魔法の習得データおよび刻印スロットを初期化しました。"), true);
        return 1;
    }

    /**
     * マナの全快または指定値設定
     */
    private static int executeMana(CommandSourceStack source, float amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("このコマンドはプレイヤーのみ実行可能です。"));
            return 0;
        }

        float max = ManaManager.getMaxMana(player);
        float target = (amount < 0.0F) ? max : Math.min(amount, max);

        ManaManager.setMana(player, target);

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.5F);

        source.sendSuccess(() -> Component.literal(String.format(
                "§6[LegendMagic] §bマナを設定しました: %.1f / %.1f", target, max)), true);
        return 1;
    }

    /**
     * 全Mob・ボスのスポーンエッグ一式（各64個）を付与
     */
    private static int executeGiveEggs(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("このコマンドはプレイヤーのみ実行可能です。"));
            return 0;
        }

        Item[] eggs = new Item[]{
                LegendMagicItems.ROLAND_SOLDIER_SPAWN_EGG.get(),
                LegendMagicItems.ROLAND_GUARD_CAPTAIN_SPAWN_EGG.get(),
                LegendMagicItems.RUNA_PRIEST_SPAWN_EGG.get(),
                LegendMagicItems.ESTABUL_SOLDIER_SPAWN_EGG.get(),
                LegendMagicItems.GASTARK_SOLDIER_SPAWN_EGG.get(),
                LegendMagicItems.DESTROYER_OF_FATE_SPAWN_EGG.get(),
                LegendMagicItems.THUNDER_WOLF_SPAWN_EGG.get(),
                LegendMagicItems.DARK_WOLF_SPAWN_EGG.get()
        };

        for (Item egg : eggs) {
            ItemStack stack = new ItemStack(egg, 64);
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        }

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        source.sendSuccess(() -> Component.literal(
                "§6[LegendMagic] §e全Mob・ボスのスポーンエッグ（全8種×各64個）をインベントリに付与しました。"), true);
        return 1;
    }

    /**
     * デバッグ・検証用テストキット一式を付与
     */
    private static int executeGiveKit(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("このコマンドはプレイヤーのみ実行可能です。"));
            return 0;
        }

        ItemStack[] kit = new ItemStack[]{
                new ItemStack(LegendMagicItems.GRIMOIRE.get()),
                new ItemStack(LegendMagicItems.ALPHA_STIGMA.get()),
                new ItemStack(LegendMagicItems.IINO_DOUE.get()),
                new ItemStack(LegendMagicItems.INNER_KNIFE.get()),
                new ItemStack(LegendMagicItems.MENORIS_COMPASS.get()),
                new ItemStack(LegendMagicItems.CORE_OF_FATE.get()),
                new ItemStack(LegendMagicItems.RING_OF_THUNDER_BEASTS.get()),
                new ItemStack(LegendMagicItems.RING_OF_THE_DARK_EMPEROR.get()),
                new ItemStack(ModBlocks.RELIC_ALTAR.get()),
                new ItemStack(ModBlocks.OFFERING_PEDESTAL.get(), 4),
                new ItemStack(LegendMagicItems.RELIC_SHARD.get(), 64),
                new ItemStack(LegendMagicItems.DANGO.get(), 64),
                new ItemStack(LegendMagicItems.GOD_DANGO_SWORD.get())
        };

        for (ItemStack stack : kit) {
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        }

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.4F);

        source.sendSuccess(() -> Component.literal(
                "§6[LegendMagic] §d検証用テストキット一式（魔導書・両魔眼・忘却欠片・祭壇・キーアイテム）を付与しました。"), true);
        return 1;
    }

    /**
     * 各国構造物またはディメンションへの直接テレポート
     */
    private static int executeTeleport(CommandSourceStack source, String destination) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("このコマンドはプレイヤーのみ実行可能です。"));
            return 0;
        }

        String target = destination.toLowerCase();
        ServerLevel currentLevel = player.serverLevel();

        switch (target) {
            case "lab" -> {
                // 魔法研究所ディメンションへ転移
                ServerLevel labLevel = player.server.getLevel(InnerKnifeItem.LAB_DIMENSION);
                if (labLevel == null) {
                    source.sendFailure(Component.literal("§c魔法研究所への次元通路が見つかりません。"));
                    return 0;
                }
                BlockPos labPos = new BlockPos(0, 32, 0);
                ensureLabPlatform(labLevel, labPos);

                player.teleportTo(labLevel, labPos.getX() + 0.5D, labPos.getY(), labPos.getZ() + 0.5D,
                        Set.of(), player.getYRot(), player.getXRot());
                source.sendSuccess(() -> Component.literal("§5[LegendMagic] 魔法研究所（magic_laboratory）へテレポートしました。"), true);
                return 1;
            }

            case "menoris" -> {
                // メノリス大陸ディメンションへ転移
                ServerLevel menorisLevel = player.server.getLevel(MenorisCompassItem.MENORIS_DIMENSION);
                if (menorisLevel == null) {
                    source.sendFailure(Component.literal("§cメノリス大陸への次元通路が見つかりません。"));
                    return 0;
                }
                BlockPos searchPos = new BlockPos((int) player.getX(), 64, (int) player.getZ());
                BlockPos targetPos = findSafeSurfacePos(menorisLevel, searchPos);

                player.teleportTo(menorisLevel, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                        Set.of(), player.getYRot(), player.getXRot());
                source.sendSuccess(() -> Component.literal("§6[LegendMagic] メノリス大陸（menoris_continent）へテレポートしました。"), true);
                return 1;
            }

            case "overworld" -> {
                // 現実世界（オーバーワールド）へ帰還
                ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
                BlockPos respawnPos = player.getRespawnPosition();
                BlockPos targetPos = (respawnPos != null) ? respawnPos : overworld.getSharedSpawnPos();
                targetPos = findSafeSurfacePos(overworld, targetPos);

                player.teleportTo(overworld, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                        Set.of(), player.getYRot(), player.getXRot());
                source.sendSuccess(() -> Component.literal("§a[LegendMagic] オーバーワールドへテレポートしました。"), true);
                return 1;
            }

            case "roland", "estabul", "runa", "gastark" -> {
                return teleportToStructure(source, player, currentLevel, target);
            }

            default -> {
                source.sendFailure(Component.literal(String.format(
                        "§c不明なテレポート先です: %s (利用可能: %s)", destination, String.join(", ", TP_DESTINATIONS))));
                return 0;
            }
        }
    }

    /**
     * 構造物への探索とテレポート処理
     */
    private static int teleportToStructure(CommandSourceStack source, ServerPlayer player, ServerLevel level, String type) {
        String structName = switch (type) {
            case "roland" -> "roland_fortress";
            case "estabul" -> "estabul_lab";
            case "runa" -> "runa_shrine";
            case "gastark" -> "gastark_fortress";
            default -> "";
        };

        String label = switch (type) {
            case "roland" -> "ローランド王国の砦";
            case "estabul" -> "エスタブール魔導研究所";
            case "runa" -> "ルーナ神殿";
            case "gastark" -> "ガスターク軍の砦";
            default -> structName;
        };

        if (level.dimension().equals(InnerKnifeItem.LAB_DIMENSION)) {
            source.sendFailure(Component.literal("§c魔法研究所内には構造物は生成されません。オーバーワールドまたはメノリス大陸で実行してください。"));
            return 0;
        }

        ResourceLocation structId = new ResourceLocation(LegendMagic.MODID, structName);
        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> structKey = ResourceKey.create(Registries.STRUCTURE, structId);
        Holder<Structure> holder = structureRegistry.getHolder(structKey).orElse(null);

        if (holder == null) {
            source.sendFailure(Component.literal("§c構造物がレジストリに見つかりません: " + structId));
            return 0;
        }

        HolderSet<Structure> holderSet = HolderSet.direct(holder);
        source.sendSuccess(() -> Component.literal(String.format("§7最寄りの %s を探索中……", label)), false);

        Pair<BlockPos, Holder<Structure>> nearest = level.getChunkSource().getGenerator().findNearestMapStructure(
                level, holderSet, player.blockPosition(), 150, false
        );

        if (nearest == null) {
            source.sendFailure(Component.literal(String.format(
                    "§c半径150チャンク以内に %s が見つかりませんでした。別の地点またはディメンションでお試しください。", label)));
            return 0;
        }

        BlockPos structPos = nearest.getFirst();
        BlockPos safePos = findSafeSurfacePos(level, structPos);

        player.teleportTo(level, safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D,
                Set.of(), player.getYRot(), player.getXRot());

        level.playSound(null, safePos.getX(), safePos.getY(), safePos.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5F, 1.0F);

        source.sendSuccess(() -> Component.literal(String.format(
                "§a[LegendMagic] %s へテレポートしました。(X=%d, Y=%d, Z=%d)",
                label, safePos.getX(), safePos.getY(), safePos.getZ())), true);

        return 1;
    }

    /**
     * 安全な地表ブロック位置を探索して返す（チャンク同期生成＆上空垂直スキャン）
     */
    private static BlockPos findSafeSurfacePos(ServerLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();

        // 1. 目的地点のチャンクを強制同期ロード＆生成保証
        level.getChunk(x >> 4, z >> 4);

        int minY = level.getMinBuildHeight() + 5;
        int maxY = Math.min(300, level.getMaxBuildHeight() - 10);

        // 2. 上空から垂直に下降スキャンして開けた最上面の固体ブロックを探索
        BlockPos landingPos = null;
        for (int y = maxY; y >= minY; y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockPos groundPos = checkPos.below();
            BlockPos headPos = checkPos.above();

            if (!level.isEmptyBlock(groundPos) && level.getBlockState(groundPos).isSolid() && !level.getBlockState(groundPos).liquid()) {
                if (level.isEmptyBlock(checkPos) && level.isEmptyBlock(headPos)) {
                    landingPos = checkPos;
                    break;
                }
            }
        }

        if (landingPos == null) {
            int surfaceY = Math.max(75, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z));
            landingPos = new BlockPos(x, surfaceY, z);
        }

        BlockPos floorPos = landingPos.below();

        // 3. 足場ブロックが空気・液体なら石レンガで足場を保証
        if (level.isEmptyBlock(floorPos) || level.getBlockState(floorPos).liquid()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.setBlockAndUpdate(floorPos.offset(dx, 0, dz), Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
        }

        // 4. 頭上2ブロックを空気で確保（窒息防止）
        level.removeBlock(landingPos, false);
        level.removeBlock(landingPos.above(), false);

        return landingPos;
    }

    /**
     * 研究所の床と祭壇の生成保証
     */
    private static void ensureLabPlatform(ServerLevel level, BlockPos center) {
        int radius = 12;
        int y = center.getY() - 1;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos floorPos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                if (level.isEmptyBlock(floorPos) || !level.getBlockState(floorPos).isSolid()) {
                    level.setBlockAndUpdate(floorPos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                }
                for (int h = 1; h <= 8; h++) {
                    BlockPos airPos = floorPos.above(h);
                    if (!level.isEmptyBlock(airPos) && level.getBlockState(airPos).getBlock() != ModBlocks.ALTAR_OF_FATE.get()) {
                        level.removeBlock(airPos, false);
                    }
                }
            }
        }

        BlockPos altarPos = center.offset(0, 0, 6);
        if (level.getBlockState(altarPos).getBlock() != ModBlocks.ALTAR_OF_FATE.get()) {
            level.setBlockAndUpdate(altarPos, ModBlocks.ALTAR_OF_FATE.get().defaultBlockState());
        }
    }
}
