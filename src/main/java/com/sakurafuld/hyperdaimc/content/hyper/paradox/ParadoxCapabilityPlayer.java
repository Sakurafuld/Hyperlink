package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxSyncCapability;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

@AutoRegisterCapability
@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ParadoxCapabilityPlayer implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<ParadoxCapabilityPlayer> TOKEN = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final String TAG_HAS_PARADOX = HYPERDAIMC + ":HasParadox";
    private final LazyOptional<ParadoxCapabilityPlayer> capability = LazyOptional.of(() -> this);

    private final Player player;
    @Nullable
    private BlockPos selected = null;
    private Deque<ParadoxChain> chains = Queues.newArrayDeque();
    private List<Sequence> sequences = Lists.newArrayList();

    private ParadoxCapabilityPlayer(Player player) {
        this.player = player;
    }

    public void updateFlag() {
        if (this.selected == null && this.chains.isEmpty() && this.sequences.isEmpty())
            this.player.getPersistentData().remove(TAG_HAS_PARADOX);
        else this.player.getPersistentData().putBoolean(TAG_HAS_PARADOX, true);
    }

    public static boolean isCapable(Player player) {
        return player.getPersistentData().getBoolean(TAG_HAS_PARADOX);
    }

    public void start(List<BlockPos> positions, boolean skipPaused, int destroyed) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;
        this.sequences.add(new Sequence(positions, skipPaused, destroyed));
        this.updateFlag();
    }

    public void tick() {
        if (!HyperCommonConfig.ENABLE_PARADOX.get() || !(ParadoxHandler.hasParadox(this.player) || this.player.getOffhandItem().is(HyperItems.PARADOX.get()))) {
            if (!this.sequences.isEmpty()) {
                this.sequences = Lists.newArrayList();
                this.updateFlag();
            }
            return;
        }

        if (!this.sequences.isEmpty()) {
            this.sequences.removeIf(sequence -> !sequence.tick(this.player));
            this.updateFlag();
        }
    }

    public boolean hasSelected() {
        return this.selected != null;
    }

    @Nullable
    public BlockPos getSelected() {
        return this.selected;
    }

    public void select(BlockPos cursor) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;
        this.selected = cursor;
        this.updateFlag();
    }

    public BlockPos unselect() {
        BlockPos selected = this.selected;
        this.selected = null;
        this.updateFlag();
        return selected;
    }

    public void unselectAndChain(BlockPos cursor) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;

        ParadoxChain chain = new ParadoxChain(this.unselect(), cursor);
        if (!this.chains.contains(chain)) {
            this.chains.add(chain);
            this.updateFlag();
        }
    }

    @Nullable
    public ParadoxChain getChain(BlockPos pos) {
        Vec3 vec = Vec3.atCenterOf(pos);
        for (ParadoxChain chain : this.chains)
            if (chain.aabb.contains(vec))
                return chain;

        return null;
    }

    public boolean hasChain(BlockPos pos) {
        return this.getChain(pos) != null;
    }

    public Deque<ParadoxChain> getChains() {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return Queues.newArrayDeque();
        return this.chains;
    }

    public void unchain(ParadoxChain chain) {
        this.chains.remove(chain);
        this.updateFlag();
    }

    public void unchain(BlockPos pos) {
        for (Iterator<ParadoxChain> iterator = this.chains.descendingIterator(); iterator.hasNext(); ) {
            ParadoxChain chain = iterator.next();
            if (chain.aabb.contains(Vec3.atCenterOf(pos))) {
                iterator.remove();
                break;
            }
        }
        this.updateFlag();
    }

    public void deleteSelection() {
        this.selected = null;
        this.chains = new ArrayDeque<>();
        this.updateFlag();
    }

    public void deleteSequences() {
        this.sequences = Lists.newArrayList();
        this.updateFlag();
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player)
            event.addCapability(identifier("paradox"), new ParadoxCapabilityPlayer(player));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == TOKEN && HyperCommonConfig.ENABLE_PARADOX.get() ? this.capability.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Sequence sequence : this.sequences)
            list.add(sequence.save());
        tag.put("Sequences", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.sequences = Lists.newArrayList();
        ListTag list = nbt.getList("Sequences", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
            this.sequences.add(new Sequence(list.getCompound(i)));
        this.updateFlag();
    }

    public void sync2Client(PacketDistributor.PacketTarget target) {
        HyperConnection.INSTANCE.send(target, new ClientboundParadoxSyncCapability(this.player.getUUID(), this.serializeNBT()));
    }

    static final class Sequence {
        private final List<BlockPos> positions;
        private final boolean skipPaused;
        private final Echo echo;
        private int ticks;

        Sequence(List<BlockPos> positions, boolean skipPaused, int destroyed) {
            this.positions = positions;
            this.skipPaused = skipPaused;
            this.echo = new Echo(destroyed);
        }

        boolean tick(Player player) {
            this.ticks++;
            BlockPos pos;
            ListIterator<BlockPos> iterator = this.positions.listIterator();
            if (iterator.hasNext()) pos = iterator.next();
            else pos = player.blockPosition();

            boolean keep = this.echo.tick(this.ticks, player, pos);

            if (!this.positions.isEmpty() && player instanceof ServerPlayer serverPlayer && this.ticks % HyperCommonConfig.PARADOX_DESTROY_PER_TICK.get() == 0) {
                int old = this.positions.size();
                long prePKC = System.currentTimeMillis();
                ParadoxHandler.gashacon(serverPlayer, () -> ParadoxHandler.captureAndTransfer(serverPlayer, () ->
                        ParadoxHandler.perfectKnockoutChaining(serverPlayer, this.positions, this.skipPaused)));
                LOG.info("[Pickdox] PKC {}ms lagged for {}blocks", System.currentTimeMillis() - prePKC, old - this.positions.size());
            }

            return keep || (!player.level().isClientSide() && !this.positions.isEmpty());
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putLongArray("Positions", this.positions.stream()
                    .mapToLong(BlockPos::asLong)
                    .toArray());
            tag.putBoolean("SkipPaused", this.skipPaused);
            tag.putInt("Echo", this.echo.size);
            tag.putInt("Ticks", this.ticks);
            return tag;
        }

        Sequence(CompoundTag tag) {
            long[] array = tag.getLongArray("Positions");
            if (array.length == 0) this.positions = Collections.emptyList();
            else {
                this.positions = new ObjectArrayList<>();
                for (long pos : array) this.positions.add(BlockPos.of(pos));
            }

            this.skipPaused = tag.getBoolean("SkipPaused");
            this.echo = new Echo(tag.getInt("Echo"));
            this.ticks = tag.getInt("Ticks");
        }
    }

    static class Echo {
        private static final List<Supplier<SoundEvent>> RANKS = List.of(
                HyperSounds.DESK_MINECRAFT_FLAP,
                HyperSounds.DESK_MINECRAFT_FLAP,
                HyperSounds.DESK_MINECRAFT_FLAP,
                HyperSounds.DESK_POP,
                HyperSounds.DESK_POP,
                HyperSounds.DESK_RESULT,
                HyperSounds.DESK_RESULT,
                HyperSounds.DESK_MINECRAFT_FINISH);

        private final int size;
        private final int max;

        Echo(int size) {
            this.size = size;
            this.max = Mth.ceil(size / (float) HyperCommonConfig.PARADOX_DESTROY_AT_ONCE.get() * (float) HyperCommonConfig.PARADOX_DESTROY_PER_TICK.get());
            LOG.debug("NewEcho size:{}, max:{}", this.size, this.max);
        }

        boolean tick(int ticks, Player player, BlockPos pos) {
            if (ticks <= this.max) {
                int maxRank = RANKS.size();
                int j = Math.min(7, Mth.floor(ticks / (maxRank - 1f)));
                float comboPitch = 1 + (j / (float) maxRank);
                int k = ticks % (maxRank - 1);
                if (j != 0 && k == 0) {
                    int l = j - 1;
                    float rankPitch = 1.5f + l % 2 * 0.5f;
                    SoundEvent rankSound = RANKS.get(l).get();
                    play(player, pos, HyperSounds.PERFECT_KNOCKOUT.get(), 0.75f, comboPitch);
                    if (rankSound != null)
                        play(player, pos, rankSound, 0.5f, rankPitch);
                } else play(player, pos, HyperSounds.PERFECT_KNOCKOUT.get(), 0.75f, comboPitch);

                return true;
            } else return false;
        }

        void play(Player player, BlockPos pos, SoundEvent event, float volume, float pitch) {
            Level level = player.level();
            if (level.isClientSide())
                player.playNotifySound(event, SoundSource.BLOCKS, volume, pitch);
            else level.playSound(player, pos, event, SoundSource.BLOCKS, volume, pitch);
        }
    }
}
