package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.mixin.MixinLevelTickEvent;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.helper.Boxes;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXErase;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXMyself;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXOpenMenu;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.client.ICuriosScreen;

import java.util.*;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class VRXHandler {
    private static final Set<String> V = Sets.newHashSet("Vault of", "Vessel of", "Vortex of", "Virtual", "Vast", "Void", "Visceral");
    private static final Set<String> R = Sets.newHashSet("Raw", "Reality", "Random", "Reverie", "Removed");
    private static final Set<String> X = Sets.newHashSet("eXistence", "eXtraction", "eXperiment", "eXcavation", "maXimum", "boX");


    private static final List<String> ALL = Lists.newArrayList();
    private static final Random RANDOM = new Random();

    static {
        for (String v : V) {
            for (String r : R) {
                for (String x : X) {
                    ALL.add(v + " " + r + " " + x);
                }
            }
        }
    }

    public static String getMake() {
        return ALL.get(RANDOM.nextInt(ALL.size()));
    }

    private static long lastErased = 0;

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
        if (event.getEntity() instanceof ServerPlayer player) {
            event.getTarget().getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.sync2Client(event.getTarget().getId(), PacketDistributor.PLAYER.with(() -> player)));
        }
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
                LOG.debug("cloned!");
            }));

            if (!present) {
                original.invalidateCaps();
            }
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.sync2Client(player.getId(), PacketDistributor.PLAYER.with(() -> player)));
        }
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(identifier("vrx"), new VRXCapability());
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void createOrErase(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getMainHandItem().is(HyperItems.VRX.get()) && !mc.player.isShiftKeyDown() && mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS) {

            boolean cancel = false;
            if (mc.hitResult instanceof BlockHitResult hit) {
                if (mc.level.getBlockState(hit.getBlockPos()).hasBlockEntity()) {
                    if (event.isUseItem()) {
                        cancel = true;
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXOpenMenu(true));
                    } else if (event.isAttack() && Util.getMillis() - lastErased > 100) {
                        VRXSavedData data = VRXSavedData.get(mc.level);
                        if (data.check(mc.player.getUUID(), hit.getBlockPos(), hit.getDirection())) {
                            cancel = true;
                            lastErased = Util.getMillis();
                            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXErase(true));
                        }
                    }
                }
            } else if (mc.hitResult instanceof EntityHitResult hit) {
                LazyOptional<VRXCapability> optional = hit.getEntity().getCapability(VRXCapability.TOKEN);
                if (optional.isPresent()) {
                    VRXCapability vrx = optional.orElseThrow(IllegalStateException::new);
                    if (event.isUseItem()) {
                        if (HyperCommonConfig.VRX_PLAYER.get() || !(hit.getEntity() instanceof Player)) {
                            cancel = true;
                            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXOpenMenu(false));
                        }
                    } else if (event.isAttack() && vrx.check(mc.player.getUUID())) {
                        cancel = true;
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXErase(false));
                    }
                }
            }
            if (cancel) {
                event.setCanceled(true);
                event.setSwingHand(true);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void createOrErase(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && screen.getMenu().getCarried().is(HyperItems.VRX.get()) && Check.INSTANCE.isIn(screen, event.getMouseX(), event.getMouseY())) {
            if (!HyperCommonConfig.ENABLE_VRX.get()) {
                return;
            }

            LocalPlayer player = Minecraft.getInstance().player;
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
    public static void render(RenderLevelStageEvent event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        List<VRXSavedData.Entry> entries = VRXSavedData.get(mc.level).getEntries();

        Set<BlockPos> posSet = Sets.newHashSet();
        if (mc.player.getMainHandItem().is(HyperItems.VRX.get()) || mc.player.getOffhandItem().is(HyperItems.VRX.get())) {
            entries.stream()
                    .filter(entry -> entry.pos.distSqr(mc.player.blockPosition()) <= mc.gameRenderer.getRenderDistance() * mc.gameRenderer.getRenderDistance())
                    .sorted(Comparator.comparingInt(entry -> entry.uuid.equals(mc.player.getUUID()) ? -1 : 1))
                    .forEach(entry -> Renders.with(poseStack, () -> {
                        boolean mine = entry.uuid.equals(mc.player.getUUID());

                        poseStack.translate(entry.pos.getX() - camera.x(), entry.pos.getY() - camera.y(), entry.pos.getZ() - camera.z());
                        renderBlock(poseStack, entry.pos, entry.face, mine, entry.xRot, entry.yRot, posSet);
                    }));
        }

        if (mc.player.containerMenu instanceof VRXMenu menu) {
            menu.execute(mc.level, block -> {
                BlockPos pos = block.getBlockPos();
                Renders.with(poseStack, () -> {
                    poseStack.translate(pos.getX() - camera.x(), pos.getY() - camera.y(), pos.getZ() - camera.z());
                    renderBlock(poseStack, block.getBlockPos(), null, true, 0, 0, Sets.newHashSet());
                });
            }, entity -> {
                AABB aabb = Boxes.identity(entity.getBoundingBox());
                Renders.with(poseStack, () -> {
                    poseStack.translate(entity.getX() - camera.x(), entity.getY() - camera.y(), entity.getZ() - camera.z());
                    poseStack.translate(aabb.getXsize() / -2, 0, aabb.getZsize() / -2);
                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), aabb, 0x80AAFFFF, face -> true);
                });
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderBlock(PoseStack poseStack, BlockPos pos, Direction face, boolean mine, float xRot, float yRot, Set<BlockPos> posSet) {
        Minecraft mc = Minecraft.getInstance();

        VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
        if (!posSet.contains(pos)) {
            shape.toAabbs().forEach(aabb ->
                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), aabb, mine ? 0x80AAFFFF : 0x40805050, direction -> true));
            posSet.add(pos);
        }

        if (face != null) {

            BakedModel model = mc.getModelManager().getModel(identifier("special/vrx"));

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
        if (face.getAxis() != axis) {
            return 0;
        } else {
            return (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? shape.max(face.getAxis()) + 0.03 : shape.min(face.getAxis()) - 0.03) - 0.5;
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderLiving(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }
        Entity entity = event.getEntity();
        entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.distanceTo(entity) > mc.gameRenderer.getRenderDistance()) {
                return;
            }

            boolean exe = mc.player.getMainHandItem().is(HyperItems.VRX.get()) || mc.player.getOffhandItem().is(HyperItems.VRX.get());
            exe |= entity == mc.player && mc.player.containerMenu.getCarried().is(HyperItems.VRX.get());
            exe &= !vrx.getEntries().isEmpty() || (mc.player != entity && mc.player.containerMenu instanceof VRXMenu menu && entity.equals(menu.execute(mc.level, b -> null, e -> e)));

            if (!exe) {
                return;
            }

            PoseStack poseStack = event.getPoseStack();
            Renders.with(poseStack, () -> {
                AABB aabb = Boxes.identity(entity.getBoundingBox());
                poseStack.translate(aabb.getXsize() / -2, 0, aabb.getZsize() / -2);
                Renders.cubeBox(poseStack.last().pose(), event.getMultiBufferSource().getBuffer(Renders.Type.HIGHLIGHT), aabb, 0x80AAFFFF, face -> true);
            });
        });
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderScreen(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && screen.getMenu().getCarried().is(HyperItems.VRX.get()) && Check.INSTANCE.isIn(screen, event.getMouseX(), event.getMouseY())) {
            if (!HyperCommonConfig.ENABLE_VRX.get()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            mc.player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                List<VRXOne> ones = vrx.getEntries().isEmpty() ? Collections.emptyList()
                        : vrx.getEntries().stream()
                        .flatMap(entry -> entry.contents.stream())
                        .toList();

                event.getGuiGraphics().renderTooltip(mc.font, Collections.singletonList(Component.translatable("tooltip.hyperdaimc.vrx.player").withStyle(style -> style.withColor(0xAAFFFF))), Optional.of(new VRXTooltip(ones)), event.getMouseX(), event.getMouseY());
            });
        }
    }

    @SubscribeEvent
    public static void creation(MixinLevelTickEvent event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }

        VRXSavedData data = VRXSavedData.get(event.getLevel());
        List<Runnable> future = Lists.newArrayList();
        for (VRXSavedData.Entry entry : Lists.newArrayList(data.getEntries())) {
            if (!event.getLevel().isLoaded(entry.pos)) {
                continue;
            }
            BlockEntity blockEntity = event.getLevel().getBlockEntity(entry.pos);
            if (blockEntity != null) {

                List<VRXOne> ones = Lists.newArrayList();
                for (VRXOne one : entry.contents) {

                    Object checked = one.prepareInsert(blockEntity, entry.face, ones);
                    // |
                    // V
                    ones.add(one);
                    if (checked != null) {
                        future.add(() -> one.insert(blockEntity, entry.face, checked));
                    }
                }
            } else {
                data.erase(entry);
            }
        }
        for (Runnable runnable : future) {
            runnable.run();
        }
    }

    @SubscribeEvent
    public static void creation(LivingEvent.LivingTickEvent event) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }

        event.getEntity().getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
            List<Runnable> future = Lists.newArrayList();
            List<VRXCapability.Entry> removal = Lists.newArrayList();
            for (VRXCapability.Entry entry : vrx.getEntries()) {
                if (!HyperCommonConfig.VRX_PLAYER.get() && event.getEntity() instanceof Player player && !player.getUUID().equals(entry.uuid)) {
                    removal.add(entry);
                    continue;
                }
                List<VRXOne> ones = Lists.newArrayList();

                for (VRXOne one : entry.contents) {

                    Object checked = one.prepareInsert(event.getEntity(), entry.face, ones);
                    ones.add(one);
                    if (checked != null) {
                        future.add(() -> one.insert(event.getEntity(), entry.face, checked));
                    }
                }
            }
            for (Runnable runnable : future) {
                runnable.run();
            }
            vrx.getEntries().removeAll(removal);
        });
    }

    public static void playSound(ServerLevel level, Vec3 position, boolean create) {
        if (create) {
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.VRX_CREATE.get(), SoundSource.PLAYERS, 1, 1);
        } else {
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.VRX_ERASE.get(), SoundSource.PLAYERS, 1, 1.5f);
        }
    }

    public enum Check {
        INSTANCE;

        @OnlyIn(Dist.CLIENT)
        public boolean isIn(AbstractContainerScreen<?> screen, double x, double y) {
            x -= screen.getGuiLeft();
            y -= screen.getGuiTop();
            if (screen instanceof InventoryScreen || (require(CURIOS).ready() && screen instanceof ICuriosScreen)) {
                return x > 26 && y > 8 && 26 + 49 >= x && 8 + 70 >= y;
            } else if (screen instanceof CreativeModeInventoryScreen) {
                return x > 73 && y > 6 && 73 + 32 >= x && 6 + 43 >= y;
            } else {
                return false;
            }
        }
    }
}
