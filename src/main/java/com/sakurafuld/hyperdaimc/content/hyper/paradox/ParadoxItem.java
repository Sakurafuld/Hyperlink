package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.infrastructure.item.AbstractGashatItem;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.ServerboundParadoxClearCreative;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

public class ParadoxItem extends AbstractGashatItem {
    public static final String TAG_UUID = "ParadoxUUID";
    private static final Component R_CLICK_TO_CLEAR = Component.translatable("tooltip.hyperdaimc.paradox.r_click_to_clear").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION = Component.translatable("tooltip.hyperdaimc.paradox.description").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_CONTROL = Component.translatable("tooltip.hyperdaimc.paradox.description.control").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_CONTROL_INVERTED = Component.translatable("tooltip.hyperdaimc.paradox.description.control_inverted").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_DROP = Component.translatable("tooltip.hyperdaimc.paradox.description.drop").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_TRANSPORT = Component.translatable("tooltip.hyperdaimc.paradox.description.transport").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_CHAIN = Component.translatable("tooltip.hyperdaimc.paradox.description.chain").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_PICK = Component.translatable("tooltip.hyperdaimc.paradox.description.pick").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_UNCHAIN = Component.translatable("tooltip.hyperdaimc.paradox.description.unchain").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_TCONSTRUCT = Component.translatable("tooltip.hyperdaimc.paradox.description.tconstruct").withStyle(ChatFormatting.GRAY);

    private static final Set<ToolAction> DIG_ACTIONS = ObjectOpenHashSet.of(ToolActions.AXE_DIG, ToolActions.PICKAXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.HOE_DIG, ToolActions.SWORD_DIG);

    public ParadoxItem(String name, Properties pProperties) {
        super(name, pProperties, 0xAA00AA, HyperCommonConfig.ENABLE_PARADOX);
    }

    @Nullable
    public static UUID getUUID(ItemStack stack) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HYPERDAIMC, Tag.TAG_COMPOUND))
            return null;
        CompoundTag modded = tag.getCompound(HYPERDAIMC);
        if (!modded.contains(TAG_UUID, Tag.TAG_INT_ARRAY))
            return null;
        return modded.getUUID(TAG_UUID);
    }

    public static UUID getOrCreateUUID(ItemStack stack) {
        UUID uuid = getUUID(stack);
        if (uuid == null) {
            CompoundTag tag = stack.getOrCreateTagElement(HYPERDAIMC);
            uuid = UUID.randomUUID();
            tag.putUUID(TAG_UUID, uuid);
        }
        return uuid;
    }

    public static void removeUUID(ItemStack stack) {
        stack.removeTagKey(HYPERDAIMC);
    }

    @Override
    protected void appendDescription(List<Component> tooltip) {
        tooltip.add(DESCRIPTION);
        if (HyperCommonConfig.PARADOX_INVERT_SHIFT.get())
            tooltip.add(DESCRIPTION_CONTROL_INVERTED);
        else tooltip.add(DESCRIPTION_CONTROL);
        tooltip.add(DESCRIPTION_DROP);
        tooltip.add(DESCRIPTION_TRANSPORT);
        tooltip.add(DESCRIPTION_CHAIN);
        tooltip.add(DESCRIPTION_PICK);
        tooltip.add(DESCRIPTION_UNCHAIN);
        if (require(TINKERSCONSTRUCT))
            tooltip.add(DESCRIPTION_TCONSTRUCT);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (getUUID(pStack) != null)
            pTooltipComponents.add(R_CLICK_TO_CLEAR);
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ParadoxCapabilityItem();
    }

    @Override
    public @Nullable CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        CompoundTag original = super.getShareTag(stack);
        if (original != null)
            tag.put("Original", original);
        stack.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradox -> {
            CompoundTag capability = paradox.serializeNBT();
            if (!capability.isEmpty())
                tag.put("Capability", capability);
        });
        return tag.isEmpty() ? null : tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt == null) {
            super.readShareTag(stack, null);
            stack.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradox -> paradox.deserializeNBT(new CompoundTag()));
        } else {
            super.readShareTag(stack, nbt.getCompound("Original"));
            stack.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradox -> paradox.deserializeNBT(nbt.getCompound("Capability")));
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        if (!this.enabled.get())
            return super.onDroppedByPlayer(item, player);

        HitResult hit = player.pick(Math.max(player.getBlockReach(), player.getEntityReach()), 1, false);
        if (hit instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {
            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(result.getBlockPos());
            if (tile != null) {
                LazyOptional<IItemHandler> capability = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, result.getDirection());
                if (capability.isPresent()) {
                    if (!level.isClientSide()) {
                        UUID uuid = getUUID(item);
                        MutableBoolean transported = new MutableBoolean();
                        if (uuid != null) {
                            ParadoxSavedData data = ParadoxSavedData.getServer();
                            ParadoxSavedData.Entry entry = data.get(uuid);
                            IItemHandler handler = capability.orElse(null);

                            boolean empty = entry.peek(stack -> {
                                if (stack.isEmpty()) return;
                                int old = stack.getCount();
                                int remaining = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false).getCount();
                                if (old > remaining) {
                                    stack.shrink(old - remaining);
                                    transported.setTrue();
                                }
                            });

                            if (empty) {
                                removeUUID(item);
                                data.remove(uuid);
                            }

                            data.sync2Client(uuid);
                        }

                        ((ServerPlayer) player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, player.getInventory().selected, item));
                        if (transported.booleanValue())
                            player.playNotifySound(HyperSounds.PERFECT_KNOCKOUT.get(), SoundSource.PLAYERS, 1, 0.25f);
                    }

                    return false;
                }
            }
        }

        return super.onDroppedByPlayer(item, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (!this.enabled.get() || pAction != ClickAction.SECONDARY || !pOther.isEmpty())
            return super.overrideOtherStackedOnMe(pStack, pOther, pSlot, pAction, pPlayer, pAccess);
        UUID uuid = getUUID(pStack);
        if (uuid != null) {
            if (!pPlayer.level().isClientSide()) {
                ParadoxSavedData data = ParadoxSavedData.getServer();
                removeUUID(pStack);
                data.remove(uuid);
                data.sync2Client(uuid);
            } else {
                if (pPlayer.isCreative())
                    Creative.INSTANCE.accept(pSlot);
                pPlayer.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.5f, 1);
                pPlayer.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.MASTER, 0.5f, 2);
            }
        }

        return true;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        if (!this.enabled.get()) return super.getTooltipImage(pStack);
        UUID uuid = getUUID(pStack);
        if (uuid == null) return super.getTooltipImage(pStack);

        return ParadoxSavedData.getClient().get(uuid).tooltip();
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!this.enabled.get()) return super.isCorrectToolForDrops(stack, state);
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (!this.enabled.get()) return super.canPerformAction(stack, toolAction);
        return DIG_ACTIONS.contains(toolAction);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        if (!this.enabled.get()) return super.getEnchantmentValue(stack);
        return 30;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (!this.enabled.get()) return super.canApplyAtEnchantingTable(stack, enchantment);
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        if (!this.enabled.get()) return super.isEnchantable(pStack);
        return true;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        if (!this.enabled.get()) return super.getEquipmentSlot(stack);
        return EquipmentSlot.MAINHAND;
    }

    @OnlyIn(Dist.CLIENT)
    enum Creative {
        INSTANCE;

        void accept(Slot slot) {
            if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen && !(slot instanceof CreativeModeInventoryScreen.SlotWrapper)) {
                int index = slot.index;
                index -= screen.isInventoryOpen() ? 0 : 9;
                HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxClearCreative(index));
            }
        }
    }
}
