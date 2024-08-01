package com.sakurafuld.hyperdaimc.content.novel;

import com.brandon3055.draconicevolution.entity.guardian.DraconicGuardianEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.sakurafuld.hyperdaimc.Deets.*;

public class NovelItem extends Item {
    public static Rarity RARITY = Rarity.create("novel", style -> style.withColor(0xCCCCCC));

    public NovelItem(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(new TranslatableComponent("item.lore.novel").withStyle(style -> style.withColor(0x999999)));
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return RARITY;
    }
}
