package com.sakurafuld.hyperdaimc.content.hyper.muteki;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.infrastructure.item.AbstractGashatItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.CURIOS;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.require;

public class MutekiItem extends AbstractGashatItem {
    private static final Component DESCRIPTION = Component.translatable("tooltip.hyperdaimc.muteki.description").withStyle(ChatFormatting.GRAY);
    private static final Component CURIOS_DESCRIPTION = Component.translatable("tooltip.hyperdaimc.muteki.curios_description").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_THEFT = Component.translatable("tooltip.hyperdaimc.muteki.description.theft").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_COMMAND = Component.translatable("tooltip.hyperdaimc.muteki.description.command").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_NOVEL = Component.translatable("tooltip.hyperdaimc.muteki.description.novel").withStyle(style -> style.withColor(0xCCCCCC));

    public MutekiItem(String name, Properties pProperties) {
        super(name, pProperties, 0xEF5030, HyperCommonConfig.ENABLE_MUTEKI);
    }

    @Override
    protected void appendDescription(List<Component> tooltip) {
        if (require(CURIOS))
            tooltip.add(CURIOS_DESCRIPTION);
        else tooltip.add(DESCRIPTION);
        tooltip.add(DESCRIPTION_THEFT);
        if (HyperCommonConfig.MUTEKI_SELECTOR.get())
            tooltip.add(DESCRIPTION_COMMAND);
        if (HyperCommonConfig.MUTEKI_NOVEL.get())
            tooltip.add(DESCRIPTION_NOVEL);
    }
}
