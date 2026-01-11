package com.sakurafuld.hyperdaimc.content.hyper.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.infrastructure.item.AbstractGashatItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ChronicleItem extends AbstractGashatItem {
    private static final Component DESCRIPTION = Component.translatable("tooltip.hyperdaimc.chronicle.description").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_UNSELECT = Component.translatable("tooltip.hyperdaimc.chronicle.description.restart").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_FREE_OWNER = Component.translatable("tooltip.hyperdaimc.chronicle.description.free_owner").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_PAUSED_OWNER = Component.translatable("tooltip.hyperdaimc.chronicle.description.paused_owner").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_INTERACTION = Component.translatable("tooltip.hyperdaimc.chronicle.description.interaction").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_PARADOX = Component.translatable("tooltip.hyperdaimc.chronicle.description.paradox").withStyle(style -> style.withColor(0xAA00AA));

    public ChronicleItem(String name, Properties pProperties) {
        super(name, pProperties, 0x00FFAA, HyperCommonConfig.ENABLE_CHRONICLE);
    }

    @Override
    protected void appendDescription(List<Component> tooltip) {
        tooltip.add(DESCRIPTION);
        tooltip.add(DESCRIPTION_UNSELECT);
        if (HyperCommonConfig.CHRONICLE_OWNER.get())
            tooltip.add(DESCRIPTION_PAUSED_OWNER);
        else tooltip.add(DESCRIPTION_FREE_OWNER);
        if (HyperCommonConfig.CHRONICLE_INTERACT.get())
            tooltip.add(DESCRIPTION_INTERACTION);
        if (HyperCommonConfig.CHRONICLE_PARADOX.get())
            tooltip.add(DESCRIPTION_PARADOX);
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!this.enabled.get()) return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
        return !pPlayer.isCreative();
    }
}
