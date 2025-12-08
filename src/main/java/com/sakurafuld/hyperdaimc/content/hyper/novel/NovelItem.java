package com.sakurafuld.hyperdaimc.content.hyper.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.infrastructure.item.AbstractGashatItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.TINKERSCONSTRUCT;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.require;

public class NovelItem extends AbstractGashatItem {
    private static final Component DESCRIPTION = Component.translatable("tooltip.hyperdaimc.novel.description").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_SNEAKING = Component.translatable("tooltip.hyperdaimc.novel.description.sneaking").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_INVERTED = Component.translatable("tooltip.hyperdaimc.novel.description_inverted").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_INVERTED_SNEAKING = Component.translatable("tooltip.hyperdaimc.novel.description_inverted.sneaking").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_WORK = Component.translatable("tooltip.hyperdaimc.novel.description.work").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_TCONSTRUCT = Component.translatable("tooltip.hyperdaimc.novel.description.tconstruct").withStyle(ChatFormatting.GRAY);


    public NovelItem(String name, Properties pProperties) {
        super(name, pProperties, 0xCCCCCC, HyperCommonConfig.ENABLE_NOVEL);
    }

    @Override
    protected void appendDescription(List<Component> tooltip) {
        if (HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
            tooltip.add(DESCRIPTION_INVERTED);
            tooltip.add(DESCRIPTION_INVERTED_SNEAKING);
        } else {
            tooltip.add(DESCRIPTION);
            tooltip.add(DESCRIPTION_SNEAKING);
        }
        tooltip.add(DESCRIPTION_WORK);
        if (require(TINKERSCONSTRUCT))
            tooltip.add(DESCRIPTION_TCONSTRUCT);
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
