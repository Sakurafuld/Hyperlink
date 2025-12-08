package com.sakurafuld.hyperdaimc.addon.ars_nouveau;

import com.google.common.collect.Lists;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.common.block.SourceBlock;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.*;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.ARS_NOUVEAU;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class VRXOneSource extends VRXOne {
    private static final ResourceLocation TEXTURE = identifier(ARS_NOUVEAU, "block/mana_still");

    private long source;
    private boolean collected;

    public VRXOneSource(long source, boolean collected) {
        this();
        this.source = source;
        this.collected = collected;
    }

    public VRXOneSource() {
        super(HyperArsNouveau.vrxSource());
    }


    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider instanceof ISourceTile || (provider instanceof LivingEntity entity && CapabilityRegistry.getMana(entity).isPresent());
    }

    public static VRXOne convert(ItemStack stack) {
        if (stack.getItem() instanceof SpellBook)
            return new VRXOneSource(Long.MAX_VALUE, false);
        else if (stack.getItem() instanceof BlockItem block && block.getBlock() instanceof SourceBlock) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("BlockEntityTag")) {
                CompoundTag tileTag = tag.getCompound("BlockEntityTag");
                if (tileTag.contains("source") && tileTag.getInt("source") > 0)
                    return new VRXOneSource(Long.MAX_VALUE, false);
            }
        }

        return EMPTY;
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        if (provider instanceof ISourceTile tile)
            return Collections.singletonList(new VRXOneSource(tile.getSource(), true));
        if (provider instanceof LivingEntity entity)
            return CapabilityRegistry.getMana(entity)
                    .map(mana -> Collections.singletonList((VRXOne) new VRXOneSource(Math.min(Long.MAX_VALUE, Math.round(mana.getCurrentMana())), true)))
                    .orElse(Collections.emptyList());
        return Collections.emptyList();
    }

    @Override
    public @Nullable Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
        if (provider instanceof ISourceTile) return "Yeah";

        if (provider instanceof LivingEntity entity && CapabilityRegistry.getMana(entity).isPresent())
            return "Okay";
        else return null;
    }

    @Override
    public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
        if (provider instanceof ISourceTile tile) {
            int count = 0;
            while (count <= 1024 && tile.canAcceptSource()) {
                tile.addSource(Integer.MAX_VALUE);
                count++;
            }
        }

        if (provider instanceof LivingEntity entity) {
            CapabilityRegistry.getMana(entity).ifPresent(mana -> {
                int count = 0;
                while (count <= 1024 && mana.getCurrentMana() < mana.getMaxMana()) {
                    mana.addMana(Double.MAX_VALUE);
                    count++;
                }
            });
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Source", this.source);
        tag.putBoolean("Collected", this.collected);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.source = tag.getLong("Source");
        this.collected = tag.getBoolean("Collected");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getQuantity() {
        return this.source;
    }

    @Override
    public void setQuantity(long quantity) {
    }

    @Override
    public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
        if (button == 0 || button == 1) {
            if (menu.getCarried().isEmpty()) {

                slot.setOne(VRXOne.EMPTY);
            } else if (slot.isEmpty()) {

                slot.setOne(this);
            } else if (slot.getOne() instanceof VRXOneItem item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried())) {

                item = new VRXOneItem(menu.getCarried().copy());
                item.stackSlot(menu, slot, button, type);
            } else if (!(slot.getOne() instanceof VRXOneSource)) {
                slot.setOne(EMPTY);
            }
        } else if (type == ClickType.CLONE && !menu.getCarried().isEmpty()) {
            if (slot.isEmpty()) {
                slot.setOne(new VRXOneItem(menu.getCarried()));
            } else if (!(slot.getOne() instanceof VRXOneItem item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried()))) {
                slot.setOne(EMPTY);
            }
        }
    }

    @Override
    public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
        if (!menu.getCarried().isEmpty() && !slot.isEmpty()) {
            int carried = menu.getCarried().getCount();
            long count = shiftDown ? carried * 10L : carried;

            if (delta < 0)
                count = -count;

            slot.grow(count);

            if (slot.isEmpty())
                slot.setOne(EMPTY);

            return true;
        } else return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        graphics.blit(x, y, 0, 16, 16, sprite);
        if (this.collected)
            Renders.slotScaledString(graphics, String.valueOf(this.getQuantity()), x, y);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty()) {
            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.source"));
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.source.description").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Ars Nouveau").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
        }
    }

    @Override
    public String toString() {
        return "[ArsSource=" + this.source + ']';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneSource that = (VRXOneSource) object;
        return source == that.source && collected == that.collected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, collected);
    }
}
