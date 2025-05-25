package com.sakurafuld.hyperdaimc.content.paradox;

import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ParadoxItem extends AbstractGashatItem {
    private static final Set<ToolAction> DIG_ACTIONS = Sets.newHashSet(ToolActions.AXE_DIG, ToolActions.PICKAXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.HOE_DIG, ToolActions.SWORD_DIG);

    public ParadoxItem(String name, Properties pProperties) {
        super(name, pProperties, 0xAA00AA, HyperServerConfig.ENABLE_PARADOX);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!this.enabled.get()) {
            return super.isCorrectToolForDrops(stack, state);
        }
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (!this.enabled.get()) {
            return super.canPerformAction(stack, toolAction);
        }
        return DIG_ACTIONS.contains(toolAction);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        if (!this.enabled.get()) {
            return super.getEnchantmentValue(stack);
        }
        return 30;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (!this.enabled.get()) {
            return super.canApplyAtEnchantingTable(stack, enchantment);
        }
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        if (!this.enabled.get()) {
            return super.isEnchantable(pStack);
        }
        return true;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        if (!this.enabled.get()) {
            return super.getEquipmentSlot(stack);
        }
        return EquipmentSlot.MAINHAND;
    }
}
