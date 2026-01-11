package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.MixinLevelTickEvent;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXErase;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXMyself;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXOpenMenu;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.client.ICuriosScreen;

import java.util.*;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class VRXHandler {
    public static final String TAG_HAS_VRX = HYPERDAIMC + ":HasVRX";
    private static final Set<String> V = Set.of("Vault of", "Vessel of", "Vortex of", "Virtual", "Vast", "Void", "Visceral");
    private static final Set<String> R = Set.of("Raw", "Reality", "Random", "Reverie", "Removed");
    private static final Set<String> X = Set.of("eXistence", "eXtraction", "eXperiment", "eXcavation", "maXimum", "boX");
    private static final List<String> ALL;
    private static final Random RANDOM = new Random();
    private static long lastErased = 0;
    private static long lastLClicked = 0;

    static {
        ImmutableList.Builder<String> all = new ImmutableList.Builder<>();
        for (String v : V)
            for (String r : R)
                for (String x : X)
                    all.add(v + " " + r + " " + x);
        ALL = all.build();
    }

    public static String getMake() {
        return ALL.get(RANDOM.nextInt(ALL.size()));
    }

    @SubscribeEvent
    public static void logIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VRXSavedData.get(player.level()).sync2Client(PacketDistributor.PLAYER.with(() -> player));
            player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.sync2Client(player.getId(), PacketDistributor.PLAYER.with(() -> player)));
        }
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VRXSavedData.get(player.level()).sync2Client(PacketDistributor.PLAYER.with(() -> player));
            player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.sync2Client(player.getId(), PacketDistributor.PLAYER.with(() -> player)));
        }
    }

    @SubscribeEvent
    public static void track(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player)
            event.getTarget().getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.sync2Client(event.getTarget().getId(), PacketDistributor.PLAYER.with(() -> player)));
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (HyperCommonConfig.VRX_KEEP.get() && event.getEntity() instanceof ServerPlayer player) {
            Player original = event.getOriginal();
            boolean present = original.getCapability(VRXCapability.TOKEN).isPresent();
            original.reviveCaps();
            original.getCapability(VRXCapability.TOKEN).ifPresent(old -> player.getCapability(VRXCapability.TOKEN).ifPresent(current -> {
                CompoundTag tag = old.serializeNBT();
                current.deserializeNBT(tag);
            }));

            if (!present) original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.sync2Client(player.getId(), PacketDistributor.PLAYER.with(() -> player)));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void createOrErase(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        HitResult hit = mc.hitResult;
        if (player.getMainHandItem().is(HyperItems.VRX.get()) && !player.isShiftKeyDown() && hit != null && hit.getType() != HitResult.Type.MISS) {

            boolean cancel = false;
            if (hit instanceof BlockHitResult result) {
                ClientLevel level = Objects.requireNonNull(mc.level);
                if (level.getBlockState(result.getBlockPos()).hasBlockEntity()) {
                    if (event.isUseItem()) {
                        cancel = true;
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXOpenMenu(true));
                    } else if (event.isAttack()) {
                        long millis = Util.getMillis();
                        if (millis - lastErased > 250 || millis - lastLClicked > 75) {
                            VRXSavedData data = VRXSavedData.get(level);
                            if (data.check(player.getUUID(), result.getBlockPos())) {
                                cancel = true;
                                lastErased = millis;
                                HyperConnection.INSTANCE.sendToServer(new ServerboundVRXErase(true));
                            }
                        }
                        lastLClicked = millis;
                    }
                }
            } else if (hit instanceof EntityHitResult result) {
                LazyOptional<VRXCapability> optional = result.getEntity().getCapability(VRXCapability.TOKEN);
                if (optional.isPresent()) {
                    VRXCapability vrx = optional.orElseThrow(IllegalStateException::new);
                    if (event.isUseItem()) {
                        if (HyperCommonConfig.VRX_PLAYER.get() || !(result.getEntity() instanceof Player)) {
                            cancel = true;
                            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXOpenMenu(false));
                        }
                    } else if (event.isAttack() && vrx.check(player.getUUID())) {
                        cancel = true;
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXErase(false));
                    }
                }
            }
            if (cancel) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void createOrErase(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!HyperCommonConfig.ENABLE_VRX.get())
            return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && screen.getMenu().getCarried().is(HyperItems.VRX.get()) && Check.INSTANCE.isIn(screen, event.getMouseX(), event.getMouseY())) {
            if (event.getButton() == InputConstants.MOUSE_BUTTON_RIGHT) {
                event.setCanceled(true);
                HyperConnection.INSTANCE.sendToServer(new ServerboundVRXMyself(true));
                player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.25f, 1);
            } else if (event.getButton() == InputConstants.MOUSE_BUTTON_LEFT) {
                player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                    if (vrx.check(player.getUUID())) {
                        event.setCanceled(true);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXMyself(false));
                        player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.25f, 1);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void cancelHighlight(RenderHighlightEvent.Block event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null)
            return;

        BlockPos pos = event.getTarget().getBlockPos();
        if (!player.getMainHandItem().is(HyperItems.VRX.get()) && !player.getOffhandItem().is(HyperItems.VRX.get()))
            return;
        if (!VRXSavedData.get(level).getEntries(pos).isEmpty())
            event.setCanceled(true);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLevelStageEvent event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null)
            return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        if (player.getMainHandItem().is(HyperItems.VRX.get()) || player.getOffhandItem().is(HyperItems.VRX.get())) {
            List<VRXSavedData.Entry> entries = VRXSavedData.get(level).getEntries();
            Set<BlockPos> posSet = Sets.newHashSet();
            Set<VRXSavedData.Entry> late = Sets.newHashSet();
            for (VRXSavedData.Entry entry : entries) {
                if (entry.pos.distSqr(player.blockPosition()) > Mth.square(mc.gameRenderer.getRenderDistance()))
                    continue;
                if (!entry.uuid.equals(player.getUUID())) late.add(entry);
                else Renders.with(poseStack, () -> renderEntry(poseStack, entry, player, level, camera, posSet));
            }

            for (VRXSavedData.Entry entry : late)
                Renders.with(poseStack, () -> renderEntry(poseStack, entry, player, level, camera, posSet));
        }

        if (player.containerMenu instanceof VRXMenu menu) {
            menu.canvas.execute(level, block -> {
                BlockPos pos = block.getBlockPos();
                Renders.with(poseStack, () -> {
                    poseStack.translate(pos.getX() - camera.x(), pos.getY() - camera.y(), pos.getZ() - camera.z());
                    renderBlock(poseStack, level, block.getBlockPos(), null, true, 0, 0, null, false);
                });
            }, entity -> {
                AABB aabb = Boxes.identity(entity.getBoundingBox());
                Renders.with(poseStack, () -> {
                    poseStack.translate(entity.getX() - camera.x(), entity.getY() - camera.y(), entity.getZ() - camera.z());
                    poseStack.translate(aabb.getXsize() / -2, 0, aabb.getZsize() / -2);
                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), aabb, 0x80AAFFFF, Predicates.alwaysTrue());
                });
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderEntry(PoseStack poseStack, VRXSavedData.Entry entry, LocalPlayer player, ClientLevel level, Vec3 camera, Set<BlockPos> posSet) {
        boolean mine = entry.uuid.equals(player.getUUID());

        poseStack.translate(entry.pos.getX() - camera.x(), entry.pos.getY() - camera.y(), entry.pos.getZ() - camera.z());
        renderBlock(poseStack, level, entry.pos, entry.face, mine, entry.xRot, entry.yRot, posSet, true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderBlock(PoseStack poseStack, ClientLevel level, BlockPos pos, @Nullable Direction face, boolean mine, float xRot, float yRot, @Nullable Set<BlockPos> posSet, boolean lines) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (posSet != null && posSet.add(pos)) {
            Renders.with(poseStack, () -> {
                shape.toAabbs().forEach(aabb ->
                        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), aabb, mine ? 0x80AAFFFF : 0xA0805050, Predicates.alwaysTrue()));
                if (lines)
                    LevelRenderer.renderShape(poseStack, Renders.getBuffer(RenderType.lineStrip()), shape, 0, 0, 0, 1, 1, 1, 1);
            });
        }

        if (face != null) {

            BakedModel model = Minecraft.getInstance().getModelManager().getModel(identifier("special/vrx"));

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.translate(edgeOffset(shape, face, Direction.Axis.X), edgeOffset(shape, face, Direction.Axis.Y), edgeOffset(shape, face, Direction.Axis.Z));

            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
            poseStack.mulPose(Axis.YP.rotationDegrees(180 - yRot));

            model = ForgeHooksClient.handleCameraTransforms(poseStack, model, ItemDisplayContext.FIXED, false);
            poseStack.translate(-0.5, -0.5, -0.5);

            Renders.model(model, poseStack, Renders.getBuffer(Sheets.translucentCullBlockSheet()), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, quad -> mine ? 0xFFFFFFFF : 0xFF805050);
        }
    }

    private static double edgeOffset(VoxelShape shape, Direction face, Direction.Axis axis) {
        if (face.getAxis() != axis)
            return 0;
        else
            return (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? shape.max(face.getAxis()) + 0.03 : shape.min(face.getAxis()) - 0.03) - 0.5;
    }

    private static boolean recursiveRendering = false;
    private static final IntOpenHashSet ERRORED = new IntOpenHashSet();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderLiving(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
        if (recursiveRendering) return;
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null)
            return;

        LivingEntity entity = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        CompoundTag persistent = entity.getPersistentData();

        if (player.distanceToSqr(entity) > Mth.square(mc.gameRenderer.getRenderDistance()))
            return;
        if (!(entity == player && player.containerMenu.getCarried().is(HyperItems.VRX.get()) || (player.getMainHandItem().is(HyperItems.VRX.get()) || player.getOffhandItem().is(HyperItems.VRX.get()))))
            return;
        if (!(persistent.getBoolean(TAG_HAS_VRX) || (player != entity && player.containerMenu instanceof VRXMenu menu && entity.equals(menu.canvas.supply(level, b -> null, e -> e)))))
            return;

        entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
            if (!vrx.getEntries().isEmpty()) {
                Renders.with(poseStack, () -> {
                    if (ERRORED.contains(entity.getId()))
                        renderEntitySimple(poseStack, entity);
                    else {
                        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
                        PoseStack sacrifice = new PoseStack();
                        sacrifice.last().pose().set(poseStack.last().pose());
                        sacrifice.last().normal().set(poseStack.last().normal());
                        try {
                            recursiveRendering = true;
                            event.getRenderer().render(entity, yaw, partialTick, sacrifice, Renders.Buffer.overrider(Renders.Buffer.colorizer(Renders.getBuffer(Renders.Type.HIGHLIGHT), 0x80AAFFFF)), LightTexture.FULL_BRIGHT);
                            event.getRenderer().render(entity, yaw, partialTick, sacrifice, Renders.Buffer.overrider(Renders.Buffer.colorizer(Renders.getBuffer(RenderType.lineStrip()), 0xFFFFFFFF)), LightTexture.FULL_BRIGHT);
                        } catch (Throwable e) {
                            LOG.info("VRXEntityRenderingErrored! {} {}", entity, e.toString());
                            ERRORED.add(entity.getId());
                        } finally {
                            recursiveRendering = false;
                        }
                    }
                });
            }
        });
    }

    private static void renderEntitySimple(PoseStack poseStack, LivingEntity entity) {
        AABB aabb = Boxes.identity(entity.getBoundingBox());
        poseStack.translate(aabb.getXsize() / -2, 0, aabb.getZsize() / -2);
        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), aabb, 0x80AAFFFF, Predicates.alwaysTrue());
        LevelRenderer.renderLineBox(poseStack, Renders.getBuffer(RenderType.lineStrip()), aabb, 1, 1, 1, 1);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderScreen(ScreenEvent.Render.Post event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && screen.getMenu().getCarried().is(HyperItems.VRX.get()) && Check.INSTANCE.isIn(screen, event.getMouseX(), event.getMouseY())) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null)
                return;

            player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                List<VRXOne> ones = vrx.getEntries().isEmpty() ? Collections.emptyList()
                        : vrx.getEntries().stream()
                        .flatMap(entry -> entry.contents().stream())
                        .toList();

                event.getGuiGraphics().renderTooltip(mc.font, Collections.singletonList(Component.translatable("tooltip.hyperdaimc.vrx.player").withStyle(style -> style.withColor(0xAAFFFF))), Optional.of(new VRXTooltip(ones)), event.getMouseX(), event.getMouseY());
            });
        }
    }

    @SubscribeEvent
    public static void creation(MixinLevelTickEvent event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;

        VRXSavedData data = VRXSavedData.get(event.getLevel());
        List<Runnable> future = Lists.newArrayList();
        data.removeIf(entry -> {
            if (entry.contents.isEmpty()) return true;
            if (event.getLevel().isLoaded(entry.pos)) {
                BlockEntity blockEntity = event.getLevel().getBlockEntity(entry.pos);
                if (blockEntity == null)
                    return true;
                List<VRXOne> ones = Lists.newArrayList();
                for (VRXOne one : entry.contents) {
                    if (event.getLevel().isClientSide() && !one.type.workOnClient())
                        continue;

                    Object checked = one.prepareInsert(blockEntity, entry.face, ones);
                    // |
                    // V
                    ones.add(one);
                    if (checked != null)
                        future.add(() -> one.insert(blockEntity, entry.face, checked));
                }
            }
            return false;
        });
//        for (VRXSavedData.Entry entry : Lists.newArrayList(data.getEntries())) {
//            if (!event.getLevel().isLoaded(entry.pos)) continue;
//
//            BlockEntity blockEntity = event.getLevel().getBlockEntity(entry.pos);
//            if (blockEntity != null) {
//
//                List<VRXOne> ones = Lists.newArrayList();
//                for (VRXOne one : entry.contents) {
//                    if (event.getLevel().isClientSide() && !one.type.workOnClient())
//                        continue;
//
//                    Object checked = one.prepareInsert(blockEntity, entry.face, ones);
//                    // |
//                    // V
//                    ones.add(one);
//                    if (checked != null) future.add(() -> one.insert(blockEntity, entry.face, checked));
//                }
//            } else data.erase(entry);
//        }
        for (Runnable runnable : future)
            runnable.run();
    }

    @SubscribeEvent
    public static void creation(LivingEvent.LivingTickEvent event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;

        LivingEntity entity = event.getEntity();
        if (!entity.isAlive() || entity.isRemoved())
            return;
        if (!entity.getPersistentData().getBoolean(TAG_HAS_VRX))
            return;

        entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
            List<Runnable> future = Lists.newArrayList();
//            List<VRXCapability.Entry> removal = Lists.newArrayList();
            vrx.getEntries().removeIf(entry -> {
                if (!HyperCommonConfig.VRX_PLAYER.get() && event.getEntity() instanceof Player player && !player.getUUID().equals(entry.uuid()))
                    return true;

                List<VRXOne> ones = Lists.newArrayList();
                for (VRXOne one : entry.contents()) {
                    if (entity.level().isClientSide() && !one.type.workOnClient())
                        continue;

                    Object checked = one.prepareInsert(event.getEntity(), entry.face(), ones);
                    ones.add(one);
                    if (checked != null)
                        future.add(() -> one.insert(event.getEntity(), entry.face(), checked));
                }
                return false;
            });
//            for (VRXCapability.Entry entry : vrx.getEntries()) {
//                if (!HyperCommonConfig.VRX_PLAYER.get() && event.getEntity() instanceof Player player && !player.getUUID().equals(entry.uuid())) {
//                    removal.add(entry);
//                    continue;
//                }
//                List<VRXOne> ones = Lists.newArrayList();
//
//                for (VRXOne one : entry.contents()) {
//
//                    Object checked = one.prepareInsert(event.getEntity(), entry.face(), ones);
//                    ones.add(one);
//                    if (checked != null) future.add(() -> one.insert(event.getEntity(), entry.face(), checked));
//                }
//            }
            for (Runnable runnable : future) runnable.run();
//            vrx.getEntries().removeAll(removal);
        });
    }

    public static void playSound(ServerLevel level, Vec3 position, boolean create) {
        if (create)
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.VRX_CREATE.get(), SoundSource.PLAYERS, 1, 1);
        else
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.VRX_ERASE.get(), SoundSource.PLAYERS, 1, 1.5f);
    }

    public enum Check {
        INSTANCE;

        @OnlyIn(Dist.CLIENT)
        public boolean isIn(AbstractContainerScreen<?> screen, double x, double y) {
            x -= screen.getGuiLeft();
            y -= screen.getGuiTop();
            if (screen instanceof InventoryScreen || (require(CURIOS) && screen instanceof ICuriosScreen))
                return x > 26 && y > 8 && 26 + 49 >= x && 8 + 70 >= y;
            else if (screen instanceof CreativeModeInventoryScreen)
                return x > 73 && y > 6 && 73 + 32 >= x && 6 + 43 >= y;
            else return false;
        }
    }
}
