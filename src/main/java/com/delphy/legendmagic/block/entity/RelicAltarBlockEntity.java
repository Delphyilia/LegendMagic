package com.delphy.legendmagic.block.entity;

import com.delphy.legendmagic.item.LegendMagicItems;
import com.delphy.legendmagic.item.RelicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RelicAltarBlockEntity extends BlockEntity {

    private ItemStack storedRelic = ItemStack.EMPTY;
    private int ritualTicks = 0;
    private boolean isRitualActive = false;

    // 東西南北の供物台座の相対オフセット (距離2)
    private static final BlockPos[] PEDESTAL_OFFSETS = new BlockPos[]{
            new BlockPos(2, 0, 0),
            new BlockPos(-2, 0, 0),
            new BlockPos(0, 0, 2),
            new BlockPos(0, 0, -2)
    };

    public RelicAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RELIC_ALTAR_BE.get(), pos, state);
    }

    public ItemStack getStoredRelic() {
        return storedRelic;
    }

    public void setStoredRelic(ItemStack stack) {
        this.storedRelic = stack;
        setChanged();
        sync();
    }

    public ItemStack removeStoredRelic() {
        if (isRitualActive) return ItemStack.EMPTY; // 儀式中は取り出し不可
        ItemStack item = this.storedRelic;
        this.storedRelic = ItemStack.EMPTY;
        setChanged();
        sync();
        return item;
    }

    public boolean isRitualActive() {
        return isRitualActive;
    }

    /**
     * 儀式を開始できるかチェック
     */
    public boolean canStartRitual() {
        if (isRitualActive) return false;
        if (storedRelic.isEmpty() || !(storedRelic.getItem() instanceof RelicItem)) return false;

        int currentLevel = RelicItem.getRelicLevel(storedRelic);
        if (currentLevel >= RelicItem.MAX_RELIC_LEVEL) return false;

        if (level == null) return false;

        // 4箇所の台座をチェック
        for (BlockPos offset : PEDESTAL_OFFSETS) {
            BlockPos pedPos = worldPosition.offset(offset);
            BlockEntity be = level.getBlockEntity(pedPos);
            if (!(be instanceof OfferingPedestalBlockEntity pedestal)) {
                return false;
            }
            ItemStack offering = pedestal.getStoredItem();
            if (offering.isEmpty() || !offering.is(LegendMagicItems.RELIC_SHARD.get())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 儀式開始
     */
    public boolean startRitual(Player player) {
        if (!canStartRitual()) return false;

        if (level != null && !level.isClientSide) {
            // 4つの台座から「遺物の欠片」を1個ずつ消費
            for (BlockPos offset : PEDESTAL_OFFSETS) {
                BlockPos pedPos = worldPosition.offset(offset);
                BlockEntity be = level.getBlockEntity(pedPos);
                if (be instanceof OfferingPedestalBlockEntity pedestal) {
                    ItemStack offering = pedestal.getStoredItem();
                    offering.shrink(1);
                    if (offering.isEmpty()) {
                        pedestal.setStoredItem(ItemStack.EMPTY);
                    } else {
                        pedestal.setStoredItem(offering);
                    }
                }
            }

            this.isRitualActive = true;
            this.ritualTicks = 0;
            setChanged();
            sync();

            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5F, 0.8F);
        }

        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RelicAltarBlockEntity entity) {
        if (!entity.isRitualActive) return;

        entity.ritualTicks++;
        ServerLevel serverLevel = (ServerLevel) level;

        double altarX = pos.getX() + 0.5D;
        double altarY = pos.getY() + 1.2D;
        double altarZ = pos.getZ() + 0.5D;

        // 供物台座から中央への光の粒子収束演出
        for (BlockPos offset : PEDESTAL_OFFSETS) {
            BlockPos pedPos = pos.offset(offset);
            double pedX = pedPos.getX() + 0.5D;
            double pedY = pedPos.getY() + 1.1D;
            double pedZ = pedPos.getZ() + 0.5D;

            Vec3 dir = new Vec3(altarX - pedX, altarY - pedY, altarZ - pedZ).normalize().scale(0.18D);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pedX, pedY, pedZ, 1, dir.x, dir.y, dir.z, 0.05D);
            serverLevel.sendParticles(ParticleTypes.GLOW, pedX, pedY, pedZ, 1, 0, 0.05D, 0, 0.01D);
        }

        // 中央での回転オーラ
        double angle = entity.ritualTicks * 0.4D;
        double rx = altarX + 0.6D * Math.cos(angle);
        double rz = altarZ + 0.6D * Math.sin(angle);
        serverLevel.sendParticles(ParticleTypes.PORTAL, rx, altarY, rz, 2, 0, 0.1D, 0, 0.05D);

        if (entity.ritualTicks % 15 == 0) {
            serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F + (entity.ritualTicks * 0.01F));
        }

        // 儀式完了 (80 ticks = 4秒)
        if (entity.ritualTicks >= 80) {
            entity.isRitualActive = false;
            entity.ritualTicks = 0;

            // 忘却欠片のレベルを +1 進化
            int currentLevel = RelicItem.getRelicLevel(entity.storedRelic);
            RelicItem.setRelicLevel(entity.storedRelic, currentLevel + 1);

            // 完了の大演出
            serverLevel.sendParticles(ParticleTypes.FLASH, altarX, altarY, altarZ, 2, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.FIREWORK, altarX, altarY, altarZ, 40, 0.5D, 0.8D, 0.5D, 0.15D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, altarX, altarY, altarZ, 30, 0.4D, 0.4D, 0.4D, 0.1D);

            serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.5F, 1.0F);
            serverLevel.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.8F, 1.2F);

            entity.setChanged();
            entity.sync();
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedRelic.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            storedRelic.save(itemTag);
            tag.put("StoredRelic", itemTag);
        }
        tag.putBoolean("IsRitualActive", isRitualActive);
        tag.putInt("RitualTicks", ritualTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("StoredRelic")) {
            this.storedRelic = ItemStack.of(tag.getCompound("StoredRelic"));
        } else {
            this.storedRelic = ItemStack.EMPTY;
        }
        this.isRitualActive = tag.getBoolean("IsRitualActive");
        this.ritualTicks = tag.getInt("RitualTicks");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
