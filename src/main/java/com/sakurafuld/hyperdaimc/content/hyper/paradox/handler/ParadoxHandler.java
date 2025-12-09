package com.sakurafuld.hyperdaimc.content.hyper.paradox.handler;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChain;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxItem;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxSavedData;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILootTableParadox;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.*;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.*;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ParadoxHandler {
    public static Player gashaconPlayer = null;
    private static long lastHeld = 0;
    private static List<ItemStack> capturedItems = null;
    private static int capturedExperiences = -1;

    public enum RenderingLevel {
        NONE,
        TERRAIN,
        ALL;

        public boolean check() {
            return this.ordinal() <= HyperCommonConfig.PARADOX_RENDERING_LEVEL.get().ordinal();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void harvestCheck(PlayerEvent.HarvestCheck event) {
        if (!event.canHarvest() && event.getEntity().getMainHandItem().is(HyperItems.PARADOX.get()))
            event.setCanHarvest(true);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !HyperCommonConfig.ENABLE_PARADOX.get())
            return;

        Player player = event.player;
        boolean held = player.getMainHandItem().is(HyperItems.PARADOX.get()) || player.getOffhandItem().is(HyperItems.PARADOX.get());
        if (held) lastHeld = player.tickCount;

        if (ParadoxCapabilityPlayer.isCapable(player))
            player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
                paradox.tick();
                if (player instanceof ServerPlayer serverPlayer) {
                    if (player.tickCount - lastHeld > HyperCommonConfig.PARADOX_FADE.get() && (paradox.hasSelected() || !paradox.getChains().isEmpty())) {
                        paradox.deleteSelection();
                        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientboundParadoxDelete());
                    }

                    if (!held && paradox.hasSelected()) {
                        paradox.unselect();
                        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), ClientboundParadoxCursor.unselect());
                    }
                }
            });
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;
        if (event.getEntity() instanceof ServerPlayer player) {
            ParadoxSavedData.getServer().sync2Client(PacketDistributor.PLAYER.with(() -> player));
            player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> paradox.sync2Client(PacketDistributor.PLAYER.with(() -> player)));
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
                paradox.deleteSelection();
                paradox.deleteSequences();
            });
            HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundParadoxDelete());
        }
    }

    @SubscribeEvent
    public static void track(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer me && event.getTarget() instanceof ServerPlayer that) {
            that.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox ->
                    paradox.sync2Client(PacketDistributor.PLAYER.with(() -> me)));
        }
    }

//    @SubscribeEvent
//    @OnlyIn(Dist.CLIENT)
//    public static void cancelRender(RenderHighlightEvent.Block event) {
//        if (!HyperCommonConfig.ENABLE_PARADOX.get())
//            return;
//        Minecraft mc = Minecraft.getInstance();
//        LocalPlayer player = Objects.requireNonNull(mc.player);
//        if (!player.getMainHandItem().is(HyperItems.PARADOX.get()) && !player.getOffhandItem().is(HyperItems.PARADOX.get()))
//            return;
//        player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
//            if (paradox.hasSelected())
//                event.setCanceled(true);
//        });
//    }

//    @SubscribeEvent
//    @OnlyIn(Dist.CLIENT)
//    public static void render(RenderLevelStageEvent event) {
//        if (!HyperCommonConfig.ENABLE_PARADOX.get())
//            return;
//        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
//            return;
//
//        Minecraft mc = Objects.requireNonNull(Minecraft.getInstance());
//        LocalPlayer player = Objects.requireNonNull(mc.player);
//        ItemStack mainHand = player.getMainHandItem();
//        ItemStack offhand = player.getOffhandItem();
//        boolean mainHanded = mainHand.is(HyperItems.PARADOX.get());
//        boolean hasParadox = mainHanded || offhand.is(HyperItems.PARADOX.get());
//        if (!hasParadox) lastNoHeld = player.tickCount;
//
//        PoseStack poseStack = event.getPoseStack();
//        Vec3 camera = event.getCamera().getPosition();
//        LazyOptional<ParadoxCapabilityPlayer> capability = LazyOptional.empty();
//        if (hasParadox) {
//            (capability = player.getCapability(ParadoxCapabilityPlayer.TOKEN)).ifPresent(paradox -> {
//                BlockPos cursor = null;
//                if (paradox.hasSelected()) {
//                    cursor = Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true);
//                    BlockPos selected = paradox.getSelected();
//                    AABB aabb = Boxes.of(selected, cursor);
//                    AABB identity = Boxes.identity(aabb);
//                    Renders.with(poseStack, () -> {
//                        poseStack.translate(aabb.minX - camera.x(), aabb.minY - camera.y(), aabb.minZ - camera.z());
//                        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, 0x45FF8B8B, Predicates.alwaysTrue());
//                        LevelRenderer.renderLineBox(poseStack, Renders.getBuffer(RenderType.lines()), identity, 0.75f, 0.75f, 1, 1);
//                    });
//                    Renders.endBatch(Renders.Type.HIGHLIGHT);
//                }
//
//                MutableObject<ParadoxChainCluster> cluster = new MutableObject<>();
//                if (mainHanded)
//                    mainHand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem ->
//                            cluster.setValue(paradoxItem.getCluster()));
//                else
//                    offhand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem ->
//                            paradoxItem.setCluster(paradoxItem.getCluster()));
//
//                if (cluster.getValue() != null) {
//                    BlockPos at = null;
//                    Direction direction = null;
//                    float partialTick = event.getPartialTick();
//                    HitResult hit = player.pick(Math.max(Objects.requireNonNull(mc.gameMode).getPickRange(), player.getEntityReach()), partialTick, HyperCommonConfig.PARADOX_HIT_FLUID.get());
//                    if (hit instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {
//                        at = result.getBlockPos();
//                        direction = result.getDirection().getOpposite();
//                    }
//
//                    if (at == null) {
//                        if (cursor == null)
//                            cursor = Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true);
//                        at = cursor;
//                    }
//
//                    if (direction == null)
//                        direction = Direction.orderedByNearest(player)[0];
//
//                    Set<ParadoxChain> chains = cluster.getValue().get(at, direction, player.getDirection());
//                    if (!chains.isEmpty()) {
//                        for (ParadoxChain chain : chains) {
//                            if (chain.visible(camera, mc.gameRenderer.getRenderDistance())) {
//                                AABB identity = Boxes.identity(chain.aabb);
//                                Renders.with(poseStack, () -> {
//                                    poseStack.translate(chain.aabb.minX - camera.x(), chain.aabb.minY - camera.y(), chain.aabb.minZ - camera.z());
//                                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, 0x45A0A0FF, Predicates.alwaysTrue());
//                                    Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.05f), 0.05f, 0xFFFF8B8B);
//                                });
//                                Renders.endBatch(Renders.Type.HIGHLIGHT);
//                            }
//                        }
//                    }
//                }
//            });
//        }
//
//        float alpha = hasParadox ? 1 : (1f - Math.min(1, (float) (lastNoHeld - lastHeld) / HyperCommonConfig.PARADOX_FADE.get()));
//        if (alpha <= 0) return;
//
//        if (!capability.isPresent())
//            capability = player.getCapability(ParadoxCapabilityPlayer.TOKEN);
//        capability.ifPresent(paradox -> {
//            Deque<ParadoxChain> chains = paradox.getChains();
//            for (ParadoxChain chain : chains) {
//                if (chain.visible(camera, mc.gameRenderer.getRenderDistance())) {
//                    AABB identity = Boxes.identity(chain.aabb);
//                    Renders.with(poseStack, () -> {
//                        poseStack.translate(chain.aabb.minX - camera.x(), chain.aabb.minY - camera.y(), chain.aabb.minZ - camera.z());
//                        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, (int) (alpha / 2f * 0xFF) << 24 | 0x45008B, Predicates.alwaysTrue());
//                        Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.05f), 0.075f, (int) (alpha * 0xFF) << 24 | 0x8B00A0);
//                    });
//                    Renders.endBatch(Renders.Type.HIGHLIGHT);
//                }
//            }
//        });
//    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void capture(EntityJoinLevelEvent event) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;

        if (!event.getLevel().isClientSide() && !event.loadedFromDisk()) {
            if (capturedItems != null) {
                ItemStack stack = ItemStack.EMPTY;
                if (event.getEntity() instanceof ItemEntity entity) {
                    event.setCanceled(true);
                    entity.discard();
                    stack = entity.getItem();
                }

                if (event.getEntity() instanceof FallingBlockEntity entity) {
                    event.setCanceled(true);
                    entity.discard();
                    stack = entity.getBlockState().getBlock().asItem().getDefaultInstance();
                }

                if (stack.isStackable()) {
                    for (ItemStack existing : capturedItems) {
                        if (stack.getItem() != existing.getItem()) continue;
                        if (!existing.isStackable()) continue;
                        if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) continue;

                        int space = existing.getMaxStackSize() - existing.getCount();
                        if (space <= 0) continue;

                        int growth = Math.min(stack.getCount(), space);
                        existing.grow(growth);
                        stack = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - growth);
                        break;
                    }
                }

                if (!stack.isEmpty()) capturedItems.add(stack);
            }

            if (capturedExperiences >= 0 && event.getEntity() instanceof ExperienceOrb entity) {
                event.setCanceled(true);
                entity.discard();
                capturedExperiences += entity.getValue();
            }
        }
    }

    public static boolean isNotParadox(Entity entity) {
        return entity == null || !Objects.equals(entity, gashaconPlayer);
    }

    public static boolean hasParadox(Player player) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return false;

        ItemStack stack = player.getMainHandItem();
        if (stack.is(HyperItems.PARADOX.get()))
            return true;
        return require(TINKERSCONSTRUCT) && TinCo.INSTANCE.hasParadox(stack);
    }

    public static boolean againstChronicle(Player player) {
        return !HyperCommonConfig.CHRONICLE_PARADOX.get() && hasParadox(player);
    }

    public static void perfectKnockout(ServerPlayer player, boolean skipPaused) {
        if (!hasParadox(player))
            return;

        ServerLevel level = player.serverLevel();
        if (player.pick(Math.max(player.getBlockReach(), player.getEntityReach()), 1, HyperCommonConfig.PARADOX_HIT_FLUID.get()) instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {
            BlockPos cursor = result.getBlockPos();
            if (!canPerfectKnockout(player, level, cursor, skipPaused))
                return;

            gashacon(player, () -> player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
                ParadoxChain start = paradox.getChain(cursor);
                if (start != null) {
                    Set<ParadoxChain> chains = ParadoxChain.find(paradox.getChains(), start);
                    startPerfectKnockout(chains, cursor, player, paradox, skipPaused);
                    chains.forEach(paradox::unchain);
                    HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundParadoxUnchainSet(chains));
                } else captureAndTransfer(player, () -> perfectKnockoutAt(player, level, cursor));
            }));
        }
    }

    public static boolean canPerfectKnockout(Player player, Level level, BlockPos pos, boolean skipPaused) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir())
            return false;
        //noinspection deprecation
        if (!HyperCommonConfig.PARADOX_HIT_FLUID.get() && (state.liquid() || state.getBlock() instanceof LiquidBlock))
            return false;
        if (!level.isLoaded(pos))
            return false;
        return skipPaused || !ChronicleHandler.isPaused(level, pos, player, true);
    }

//    private static Pair<List<BlockPos>, Set<ParadoxChain>> findAndUnchain(ServerPlayer player, ParadoxCapabilityPlayer paradox, Queue<ParadoxChain> chains, BlockPos cursor, ParadoxChain start, boolean skipPaused) {
//        ObjectOpenHashSet<ParadoxChain> foundChains = new ObjectOpenHashSet<>();
//        ArrayDeque<ParadoxChain> frontier = Queues.newArrayDeque();
//        ObjectOpenHashSet<ParadoxChain> visited = new ObjectOpenHashSet<>();
//
//        frontier.add(start);
//        visited.add(start);
//        while (!frontier.isEmpty()) {
//            ParadoxChain current = frontier.poll();
//            foundChains.add(current);
//            paradox.unchain(current);
//
//            for (ParadoxChain other : chains) {
//                if (!current.aabb.intersects(other.aabb))
//                    continue;
//                if (visited.add(other))
//                    frontier.add(other);
//            }
//        }
//
//        ServerLevel level = player.serverLevel();
//        ArrayDeque<BlockPos> next = new ArrayDeque<>();
//        ObjectOpenHashSet<BlockPos> checked = new ObjectOpenHashSet<>();
//        ObjectArrayList<BlockPos> foundPos = new ObjectArrayList<>();
//
//        next.add(cursor);
//        checked.add(cursor);
//        foundPos.add(cursor);
//        while (!next.isEmpty()) {
//            ArrayDeque<BlockPos> currents = next;
//            next = new ArrayDeque<>();
//
//            for (BlockPos pos : currents) {
//                for (Direction face : DIRECTIONS) {
//                    BlockPos relative = pos.relative(face);
//                    if (checked.add(relative)) {
//                        Vec3 center = Vec3.atCenterOf(relative);
//                        boolean contain = canPerfectKnockout(player, level, relative, skipPaused);
//                        for (ParadoxChain chain : foundChains) {
//                            if (!chain.aabb.contains(center))
//                                continue;
//                            next.add(relative);
//                            if (contain) foundPos.add(relative);
//                        }
//                    }
//                }
//            }
//        }
//
//        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundParadoxUnchainSet(foundChains));
//        return Pair.of(foundPos, foundChains);
//    }

    public static void gashacon(ServerPlayer player, Runnable runnable) {
        gashaconPlayer = player;
        runnable.run();
        gashaconPlayer = null;
    }

    public static void captureAndTransfer(ServerPlayer player, Runnable runnable) {
        capturedItems = new ObjectArrayList<>();
        capturedExperiences = 0;

        runnable.run();

        List<ItemStack> items = capturedItems;
        int experiences = capturedExperiences;
        capturedItems = null;
        capturedExperiences = -1;

        ServerLevel level = player.serverLevel();
        if (!items.isEmpty()) {
            ItemStack pickdox = ItemStack.EMPTY;
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.is(HyperItems.PARADOX.get())) pickdox = mainHand;
            else {
                ItemStack offhand = player.getOffhandItem();
                if (offhand.is(HyperItems.PARADOX.get())) pickdox = offhand;
            }

            items.sort(Calculates.LOWEST_TO_HIGHEST);
            if (pickdox.isEmpty()) {
                for (ItemStack item : items) {
                    ItemEntity entity = new ItemEntity(level, player.getX(), player.getEyeY(), player.getZ(), item);
                    entity.setNoPickUpDelay();
                    level.addFreshEntity(entity);
                    entity.playerTouch(player);
                }
            } else {
                ListIterator<ItemStack> iterator = items.listIterator();
                while (iterator.hasNext()) {
                    ItemStack item = iterator.next();
                    ItemEntity entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), item);
                    entity.setNoPickUpDelay();
                    level.addFreshEntity(entity);

                    boolean instabuilt = player.getAbilities().instabuild;
                    player.getAbilities().instabuild = false;
                    entity.playerTouch(player);
                    player.getAbilities().instabuild = instabuilt;

                    if (entity.isRemoved()) iterator.remove();
                    else iterator.set(entity.getItem().copy());
                    entity.discard();
                }

                if (!items.isEmpty()) {
                    UUID uuid = ParadoxItem.getOrCreateUUID(pickdox);
                    ParadoxSavedData data = ParadoxSavedData.getServer();
                    data.add(uuid, items);
                    data.sync2Client(uuid);
                }
            }
        }

        if (experiences > 0) {
            ExperienceOrb experience = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), experiences);
            level.addFreshEntity(experience);
            player.takeXpDelay = 0;
            experience.playerTouch(player);
        }
    }

    public static void startPerfectKnockout(Set<ParadoxChain> allChains, BlockPos cursor, ServerPlayer player, ParadoxCapabilityPlayer paradox, boolean skipPaused) {
        List<BlockPos> allPos = ParadoxChain.connect(allChains, cursor, player, skipPaused);
        if (allPos.isEmpty())
            return;
        int maxSize = allPos.size();
        captureAndTransfer(player, () -> {
            long prePKC = System.currentTimeMillis();
            perfectKnockoutChaining(player, allPos, skipPaused);
            LOG.info("[Pickdox] PKC {}ms lagged for {}blocks", System.currentTimeMillis() - prePKC, maxSize - allPos.size());
        });
        paradox.start(allPos, skipPaused, maxSize);
        paradox.sync2Client(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
    }

    public static void perfectKnockoutChaining(ServerPlayer player, List<BlockPos> area, boolean skipPaused) {
        ServerLevel level = player.serverLevel();
        Long2IntOpenHashMap blocks = new Long2IntOpenHashMap();

        final int max = HyperCommonConfig.PARADOX_DESTROY_AT_ONCE.get();
        ListIterator<BlockPos> iterator = area.listIterator();
        while (iterator.hasNext()) {
            if (blocks.size() >= max)
                break;
            BlockPos pos = iterator.next();
            iterator.remove();
            if (!canPerfectKnockout(player, level, pos, skipPaused))
                continue;
            BlockState currentBlock = level.getBlockState(pos);

            blocks.put(pos.asLong(), Block.getId(currentBlock));
            FluidState currentFluid = level.getFluidState(pos);
            BlockEntity currentTile = level.getBlockEntity(pos);
            BlockState legacyBlock = currentBlock.getBlock() instanceof LiquidBlock ? Blocks.AIR.defaultBlockState() : currentFluid.createLegacyBlock();

            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, currentBlock, player);
            MinecraftForge.EVENT_BUS.post(event);

            boolean creative = player.isCreative();

            if (!creative) player.getMainHandItem().mineBlock(level, currentBlock, pos, player);
            currentBlock.onDestroyedByPlayer(level, pos, player, !creative, currentFluid);
            perfectKnockoutAbsolute(level, pos, currentBlock, legacyBlock);
            currentBlock.getBlock().destroy(level, pos, currentBlock);

            if (!creative) {
                float exhaustion = player.getFoodData().getExhaustionLevel();
                currentBlock.getBlock().playerDestroy(level, player, pos, currentBlock, currentTile, player.getMainHandItem());
                player.getFoodData().setExhaustion(exhaustion);

                ResourceLocation loot = currentBlock.getBlock().getLootTable();
                if (loot == BuiltInLootTables.EMPTY || (level.getServer().getLootData().getLootTable(loot) instanceof ILootTableParadox table && table.hyperdaimc$hasNoDrop())) {
                    ItemStack stack = new ItemStack(currentBlock.getBlock());
                    if (!stack.isEmpty()) Block.popResource(level, pos, stack);
                }

                int experience = event.getExpToDrop();
                if (experience > 0) currentBlock.getBlock().popExperience(level, pos, experience);
            }
        }

        if (!blocks.isEmpty()) {
            HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundPerfectKnockedoutUpdates(blocks.keySet().toLongArray()));
            if (!HyperCommonConfig.PARADOX_NO_CHAIN_PARTICLES.get())
                HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundPerfectKnockedoutParticles(blocks));
        }
    }

    private static void perfectKnockoutAt(ServerPlayer player, ServerLevel level, BlockPos cursor) {
        BlockState currentBlock = level.getBlockState(cursor);
        FluidState currentFluid = level.getFluidState(cursor);
        BlockEntity currentTile = level.getBlockEntity(cursor);
        BlockState legacyBlock = currentBlock.getBlock() instanceof LiquidBlock ? Blocks.AIR.defaultBlockState() : currentFluid.createLegacyBlock();

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, cursor, currentBlock, player);
        MinecraftForge.EVENT_BUS.post(event);

        boolean creative = player.isCreative();

        if (!creative) player.getMainHandItem().mineBlock(level, currentBlock, cursor, player);
        currentBlock.onDestroyedByPlayer(level, cursor, player, !creative, currentFluid);
        perfectKnockoutAbsolute(level, cursor, currentBlock, legacyBlock);
        currentBlock.getBlock().destroy(level, cursor, currentBlock);

        HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(cursor)), new ClientboundPerfectKnockedoutParticles(new Long2IntOpenHashMap(new long[]{cursor.asLong()}, new int[]{Block.getId(currentBlock)})));

        if (!creative) {
            float exhaustion = player.getFoodData().getExhaustionLevel();
            currentBlock.getBlock().playerDestroy(level, player, cursor, currentBlock, currentTile, player.getMainHandItem());
            player.getFoodData().setExhaustion(exhaustion);

            ResourceLocation loot = currentBlock.getBlock().getLootTable();
            if (loot == BuiltInLootTables.EMPTY || (level.getServer().getLootData().getLootTable(loot) instanceof ILootTableParadox paradox && paradox.hyperdaimc$hasNoDrop())) {
                ItemStack stack = new ItemStack(currentBlock.getBlock());
                if (!stack.isEmpty()) Block.popResource(level, cursor, stack);
            }

            int experience = event.getExpToDrop();
            if (experience > 0) currentBlock.getBlock().popExperience(level, cursor, experience);
        }
        player.connection.send(new ClientboundBlockUpdatePacket(cursor, legacyBlock));
        level.playSound(null, cursor, HyperSounds.PERFECT_KNOCKOUT.get(), SoundSource.BLOCKS, 0.75f, 1);
    }

    private static void perfectKnockoutAbsolute(ServerLevel level, BlockPos pos, BlockState old, BlockState legacy) {
        int sectionX = pos.getX() & 15;
        int sectionY = pos.getY() & 15;
        int sectionZ = pos.getZ() & 15;
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getSection(chunk.getSectionIndex(pos.getY())).setBlockState(sectionX, sectionY, sectionZ, legacy);
        level.markAndNotifyBlock(pos, chunk, old, level.getBlockState(pos), Block.UPDATE_ALL, 512);
        level.getLightEngine().checkBlock(pos);
        chunk.setUnsaved(true);
    }

    enum TinCo {
        INSTANCE;

        boolean hasParadox(ItemStack stack) {
            if (stack.getItem() instanceof IModifiable) {
                ToolStack tool = ToolStack.from(stack);
                return !tool.isBroken() && tool.getModifierLevel(HyperModifiers.PARADOX.getId()) > 0;
            } else return false;
        }
    }
}
