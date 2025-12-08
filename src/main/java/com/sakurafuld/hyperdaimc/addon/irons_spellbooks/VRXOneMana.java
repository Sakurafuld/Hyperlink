package com.sakurafuld.hyperdaimc.addon.irons_spellbooks;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.*;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
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

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class VRXOneMana extends VRXOne {
    private static final ResourceLocation TEXTURE = identifier("gui/vrx/irons_spellbooks_mana");

    private int mana;
    private boolean collected;

    public VRXOneMana(int mana, boolean collected) {
        this();
        this.mana = mana;
        this.collected = collected;
    }

    public VRXOneMana() {
        super(HyperIronsSpellbooks.vrxMana());
    }

    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider instanceof LivingEntity entity && entity.getAttributeValue(AttributeRegistry.MAX_MANA.get()) > 0;
    }

    public static VRXOne convert(ItemStack stack) {
        if (stack.getItem() instanceof SpellBook)
            return new VRXOneMana(Integer.MAX_VALUE, false);
        else return EMPTY;
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        if (provider instanceof LivingEntity entity && entity.getAttributeValue(AttributeRegistry.MAX_MANA.get()) > 0) {
            MagicData data = MagicData.getPlayerMagicData(entity);
            return Collections.singletonList(new VRXOneMana(Math.round(Math.min(Integer.MAX_VALUE, data.getMana())), true));
        }

        return Collections.emptyList();
    }

    @Override
    public @Nullable Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
        if (provider instanceof LivingEntity entity) {
            double max = entity.getAttributeValue(AttributeRegistry.MAX_MANA.get());
            if (max > 0) {
                MagicData data = MagicData.getPlayerMagicData(entity);
                if (data.getMana() < max)
                    return "Yeah";
            }
        }

        return null;
    }

    @Override
    public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
        if (provider instanceof LivingEntity entity) {
            double max = entity.getAttributeValue(AttributeRegistry.MAX_MANA.get());
            MagicData data = MagicData.getPlayerMagicData(entity);
            int count = 0;
            while (count <= 1024 && data.getMana() < max) {
                data.addMana(Float.MAX_VALUE);
                count++;
            }
            if (count > 0 && entity instanceof ServerPlayer player)
                Messages.sendToPlayer(new ClientboundSyncMana(data), player);
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", this.mana);
        tag.putBoolean("Collected", this.collected);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.mana = tag.getInt("Mana");
        this.collected = tag.getBoolean("Collected");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getQuantity() {
        return this.mana;
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
            } else if (!(slot.getOne() instanceof VRXOneMana)) {
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
            //noinspection DataFlowIssue
            Renders.slotScaledString(graphics, String.valueOf(this.getQuantity()), x, y, ChatFormatting.AQUA.getColor());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty()) {
            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.irons_spellbooks_mana"));
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.irons_spellbooks_mana.description").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Iron's Spells 'n Spellbooks").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
        }
    }

    @Override
    public String toString() {
        return "[BotaniaMana=" + this.mana + ']';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneMana that = (VRXOneMana) object;
        return mana == that.mana && collected == that.collected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mana, collected);
    }
}
