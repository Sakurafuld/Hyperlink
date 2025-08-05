package com.sakurafuld.hyperdaimc.content.hyper.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NovelItem extends AbstractGashatItem {

    public NovelItem(String name, Properties pProperties) {
        super(name, pProperties, 0xCCCCCC, HyperCommonConfig.ENABLE_NOVEL);
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!this.enabled.get()) {
            return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
        }
        return !pPlayer.isCreative();
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
