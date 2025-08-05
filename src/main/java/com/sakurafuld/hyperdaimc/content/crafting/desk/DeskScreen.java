package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.api.content.IScreenVFX;
import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.desk.ServerboundDeskDoneAnimation;
import com.sakurafuld.hyperdaimc.network.desk.ServerboundDeskLockRecipe;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class DeskScreen extends AbstractContainerScreen<DeskMenu> {
    private static final ResourceLocation BACKGROUND = identifier("textures/gui/container/desk.png");
    private static final Component LOCK = Component.translatable("tooltip.hyperdaimc.desk.lock");
    private static final Component UNLOCK = Component.translatable("tooltip.hyperdaimc.desk.unlock").withStyle(ChatFormatting.GRAY);
    private static final Component MINECRAFT = Component.translatable("tooltip.hyperdaimc.desk.minecrafting");
    private final Set<IScreenVFX> visualEffects = Sets.newHashSet();
    private final Set<IScreenVFX> temporaryEffects = Sets.newHashSet();
    private final Set<Integer> vanished = Sets.newHashSet();
    private Data data = Data.EMPTY;
    private boolean visualCrafting = false;
    public int canCraftTicks = 0;
    public Vec2 resultPos = null;
    public Vec2 resultOldPos = null;
    public Vec2 resultMove = Vec2.ZERO;
    public float resultSize = 1;
    public float resultOldSize = 1;
    public float resultRot = 0;
    public float resultOldRot = 0;
    private int standby = 0;

    public DeskScreen(DeskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 296;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void containerTick() {
        Slot result = this.getMenu().getSlot(0);
        if (!result.hasItem()) {
            this.clear();
            this.data = Data.EMPTY;
        } else {
            if (this.data.result == result.getItem()) {
                this.data.tick(index -> {
                    if (this.data.indexes.lastIndexOf(index) == this.data.indexes.size() - 1) {
                        this.visualCrafting = true;
                    }
                    this.vanished.add(index);
                    Slot slot = this.getMenu().getSlot(index);
                    this.visualEffects.add(new DeskItemVFX(this, slot, result));
                    slot.getItem().setPopTime(5);
                    this.getMinecraft().player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.MASTER, 0.5f, 0.01f);
                });
            } else {
                this.clear();
                ItemStackHandler handler = this.getMenu().access.evaluate(((level, pos) -> level.getBlockEntity(pos, HyperBlockEntities.DESK.get()).map(desk -> desk.inventory)))
                        .flatMap(optional -> optional)
                        .orElseThrow();
                List<Integer> indexes = Lists.newArrayList();
                for (int index = 0; index < handler.getSlots(); index++) {
                    if (!handler.getStackInSlot(index).isEmpty()) {
                        indexes.add(index + 1);
                    }
                }

                this.data = new Data(result.getItem(), 2, indexes);
            }
        }

        this.visualEffects.removeIf(vfx -> !vfx.tick());
        this.visualEffects.addAll(this.temporaryEffects);
        this.temporaryEffects.clear();

        for (int index = 0; index < 82; index++) {
            ItemStack stack = this.getMenu().getSlot(index).getItem();
            if (stack.getPopTime() > 0) {
                stack.setPopTime(stack.getPopTime() - 1);
            }
        }

        if (this.getMenu().canCraft) {
            ++this.canCraftTicks;
            --this.standby;
            if (this.canCraftTicks <= 37) {
                if (this.resultPos == null) {
                    this.resultPos = Vec2.ZERO;
                    this.resultMove = new Vec2(0, -6.5f);
                    Random random = new Random();
                    this.addTriangleFX(random, new Vec2(result.x + 6, result.y + 8), 0.4f, 4.5f);
                    this.addTriangleFX(random, new Vec2(result.x + 10, result.y + 8), 0.4f, 4.5f);
                    this.getMinecraft().player.playNotifySound(HyperSounds.DESK_RESULT.get(), SoundSource.MASTER, 0.675f, 1);
                }

                this.resultOldPos = this.resultPos;
                this.resultOldSize = this.resultSize;
                this.resultOldRot = this.resultRot;

                if (!this.getMenu().isMinecraft()) {
                    this.resultPos = this.resultPos.add(this.resultMove);
                    if (this.resultPos.y > 0) {
                        this.addTriangleFX(new Random(), new Vec2(result.x + 8, result.y + 12), 0.15f, 1.5f);
                        this.resultPos = Vec2.ZERO;
                        if (this.canCraftTicks < 25) {
                            this.resultMove = new Vec2(0, -3);
                        } else if (this.canCraftTicks < 33) {
                            this.resultMove = new Vec2(0, -2.5f);
                        } else {
                            this.resultMove = new Vec2(0, -1.3125f);
                        }
                    }

                    this.resultMove = new Vec2(this.resultMove.x, this.resultMove.y + 0.9f);

                    this.resultSize = (float) Calculates.curve(Math.min(1, this.canCraftTicks / 7d), 1, 3, 0.25, 1);
                }

                this.resultRot = (float) Calculates.curve(Math.min(1, this.canCraftTicks / 6d), 0, -180, 450, 360);
            } else {
                this.resultPos = null;
            }

            if (this.getMenu().isMinecraft() && this.canCraftTicks % 7 == 6) {
                Random random = new Random();
                for (int count = 0; count < 3; count++) {
                    float x = Mth.cos((float) Math.toRadians(random.nextInt(360)));
                    float y = Mth.sin((float) Math.toRadians(random.nextInt(360)));

                    x *= Mth.lerp(random.nextFloat(), 1, 2) * (random.nextBoolean() ? -1 : 1);

                    this.temporaryEffects.add(new DeskDustVFX(new Vec2(110, 195), new Vec2(x, y - 1.25f).scale(random.nextInt(2, 4)), random.nextInt(8, 16), random.nextFloat() * 20));
                }
                this.getMinecraft().player.playNotifySound(HyperSounds.DESK_MINING.get(), SoundSource.MASTER, 0.5f, 0.75f + random.nextFloat() * 0.2f);

                if (this.canCraftTicks > 6 && this.canCraftTicks % 28 == 6) {
                    this.getMinecraft().player.playNotifySound(HyperSounds.DESK_RESULT.get(), SoundSource.MASTER, 0.25f, 1.5f);
                    this.standby = 6;
                }
            }
        } else {
            this.resultPos = null;
            this.canCraftTicks = 0;
        }
    }

    private void clear() {
        this.clearItemVFX();
        this.vanished.clear();
        this.visualCrafting = false;
        this.getMenu().canCraft = false;
        HyperConnection.INSTANCE.sendToServer(new ServerboundDeskDoneAnimation(this.getMenu().containerId, false));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.isHovering(106, 189, 15, 12, pMouseX, pMouseY)) {
            Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock = new Object2ObjectOpenHashMap<>();
            if (!hasShiftDown()) {
                for (int index = 0; index < 81; index++) {
                    ItemStack stack = this.getMenu().getSlot(index + 1).getItem();
                    if (!stack.isEmpty()) {
                        lock.computeIfAbsent(stack.getItem(), item -> new IntAVLTreeSet()).add(index);
                    }
                }
            }

            this.getMenu().access.execute((level, pos) -> level.getBlockEntity(pos, HyperBlockEntities.DESK.get()).ifPresent(desk ->
                    desk.lockRecipe(lock)));
            HyperConnection.INSTANCE.sendToServer(new ServerboundDeskLockRecipe(this.getMenu().containerId, lock));

            this.getMinecraft().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.MASTER, 1, 1);
            return true;
        } else {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderCovers(pGuiGraphics);
        this.renderParticle(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(BACKGROUND, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight, 512, 512);

        if (this.getMenu().isMinecraft()) {
            if (this.getMenu().canCraft) {
                int x = this.getGuiLeft() + 113;
                int y = this.getGuiTop() + 189;
                float cos = Mth.cos(this.canCraftTicks);
                Renders.with(pGuiGraphics.pose(), () -> {

                    pGuiGraphics.pose().translate(x + 6, y + 6, 0);
                    pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(cos * 10));
                    pGuiGraphics.pose().translate(-x - 6, -y - 6, 0);
                    pGuiGraphics.pose().translate(cos * 0.75, 0, 0);
                    pGuiGraphics.blit(BACKGROUND, x, y, 0, 176, 12, 8, 8, 512, 512);

                });

                pGuiGraphics.blit(BACKGROUND, this.getGuiLeft() + 106, this.getGuiTop() + 191, 0, 176, 20, 10, 10, 512, 512);

            } else {
                int size = 12;
                if (this.visualCrafting) {
                    size = Math.round(this.visualEffects.stream()
                            .filter(vfx -> vfx instanceof DeskItemVFX)
                            .count() / (float) this.data.indexes.size() * 12f);
                }

                if (size > 0) {
                    pGuiGraphics.blit(BACKGROUND, this.getGuiLeft() + 106, this.getGuiTop() + 189, 0, 176, 0, 15, 12, 512, 512);
                }
                if (12 > size) {
                    int offset;
                    if (10 > size) {
                        offset = Math.max(0, size - 2);
                        pGuiGraphics.blit(BACKGROUND, this.getGuiLeft() + 113, this.getGuiTop() + 189 + offset, 0, 176, 12 + offset, 8, 8 - offset, 512, 512);

                    }

                    offset = Math.max(0, Math.min(10, size) - 1);
                    pGuiGraphics.blit(BACKGROUND, this.getGuiLeft() + 106, this.getGuiTop() + 190 + offset + 1, 0, 176, 20 + offset, 10, 10 - offset, 512, 512);
                }
            }
        } else {
            pGuiGraphics.blit(BACKGROUND, this.getGuiLeft() + 106, this.getGuiTop() + 189, 0, 176, 0, 15, 12, 512, 512);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        if (this.isHovering(106, 189, 15, 12, pX, pY)) {
            List<Component> tooltip = Lists.newArrayList();

            if (this.getMenu().isMinecraft() && this.getMenu().canCraft) {
                tooltip.add(MINECRAFT);
            }

            tooltip.add(LOCK.copy().withStyle(tooltip.isEmpty() ? ChatFormatting.WHITE : ChatFormatting.GRAY));
            tooltip.add(UNLOCK);

            pGuiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), pX, pY);
        } else {
            super.renderTooltip(pGuiGraphics, pX, pY);
        }
    }

    private void renderCovers(GuiGraphics graphics) {
        this.getMenu().access.execute((level, pos) -> level.getBlockEntity(pos, HyperBlockEntities.DESK.get()).ifPresent(desk -> desk.foeEachLocked((item, set) -> {
            ItemStack stack = item.getDefaultInstance();
            set.forEach(index -> {
                Slot slot = this.getMenu().getSlot(index + 1);
                int color;
                if (!slot.hasItem()) {
                    graphics.renderFakeItem(stack, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y);
                    color = 0x8B8B8B00;
                } else {
                    color = 0x458BFF00;
                }
                renderCover(graphics, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y, 16, color);

            });
        })));
        if (!this.getMenu().getSlot(0).getItem().isEmpty()) {
            for (int index : this.vanished) {
                Slot slot = this.getMenu().getSlot(index);
                renderCover(graphics, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y, 16);
            }

            int size = 24;
            if (this.visualCrafting) {
                size = Math.round(this.visualEffects.stream()
                        .filter(vfx -> vfx instanceof DeskItemVFX)
                        .count() / (float) this.data.indexes.size() * 24f);
            }
            if (size <= 0 && !this.getMenu().canCraft) {
                this.getMenu().canCraft = true;
                HyperConnection.INSTANCE.sendToServer(new ServerboundDeskDoneAnimation(this.getMenu().containerId, true));
            }

            if (this.getMenu().isMinecraft()) {
                size = 24;
            }

            if (size > 0) {
                int color = 0x8B8B8B8B;
                if (this.standby > 0) {
                    int partial = Math.round(Mth.lerp(this.standby / 6f, 0x8B, 0xE0));
                    color = (partial << 24) | (partial << 16) | (partial << 8) | partial;
                }
                int half = size / 2;
                int x = this.getGuiLeft() + 76 + 12 - half;
                int y = this.getGuiTop() + 184 + 12 - half;
                renderCover(graphics, x, y, size, color);
            }
        }
    }

    private static void renderCover(GuiGraphics graphics, int x, int y, int size, int color) {
        Renders.with(graphics.pose(), () -> {
            graphics.pose().translate(0, 0, 300);
            graphics.fill(x, y, x + size, y + size, color);
        });
    }

    private static void renderCover(GuiGraphics graphics, int x, int y, int size) {
        renderCover(graphics, x, y, size, 0x8B8B8B8B);
    }

    private void renderParticle(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Renders.with(graphics.pose(), () -> {
            graphics.pose().translate(this.getGuiLeft(), this.getGuiTop(), 0);
            this.visualEffects.forEach(vfx -> Renders.with(graphics.pose(), () ->
                    vfx.render(graphics, mouseX, mouseY, partialTick)));
        });
    }

    private void clearItemVFX() {
        Random random = new Random();
        this.visualEffects.removeIf(vfx -> {
            if (vfx instanceof DeskItemVFX itemVFX) {
                this.addTriangleFX(random, itemVFX.getPosition().add(8), 0.25f, 1);
                return true;
            } else {
                return false;
            }
        });
    }

    public void addTriangleFX(Random random, Vec2 position, float volume, float pitch) {
        for (int count = 0; count < 3; count++) {
            position = position.add(new Vec2(random.nextFloat(-1, 2), random.nextFloat(-1, 2)));
            float x = Mth.cos((float) Math.toRadians(random.nextInt(360)));
            float y = Mth.sin((float) Math.toRadians(random.nextInt(360)));

            x *= Mth.lerp(random.nextFloat(), 1, 3) * (random.nextBoolean() ? -1 : 1);

            this.temporaryEffects.add(new DeskTriangleVFX(position, new Vec2(x, y - 1.25f).scale(random.nextInt(3, 5)), random.nextInt(6, 10), random.nextFloat() * 180, (0xFF << 24) | random.nextInt(0xFFFFFF)));
        }
        this.getMinecraft().player.playNotifySound(HyperSounds.DESK_POP.get(), SoundSource.MASTER, volume, pitch);
    }

    public Data getData() {
        return this.data;
    }

    public static class Data {
        private static final Data EMPTY = new Data(ItemStack.EMPTY, 0, Collections.emptyList());
        private final ItemStack result;
        private final int frequency;
        private final List<Integer> indexes;
        private int ticks = 0;
        private int count = 0;

        private Data(ItemStack result, int frequency, List<Integer> indexes) {
            this.result = result;
            this.frequency = frequency;
            this.indexes = indexes;
        }

        private void tick(Consumer<Integer> particular) {
            if (this != EMPTY && ++this.ticks <= this.frequency * this.indexes.size()) {
                if (this.ticks % this.frequency == 0) {
                    particular.accept(this.indexes.get(this.count++));
                }
            }
        }

        public int getFrequency() {
            return this.frequency;
        }

        public List<Integer> getIndexes() {
            return this.indexes;
        }
    }
}
