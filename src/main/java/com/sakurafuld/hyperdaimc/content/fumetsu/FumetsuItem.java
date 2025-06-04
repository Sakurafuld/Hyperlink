package com.sakurafuld.hyperdaimc.content.fumetsu;

import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.helper.Writes;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class FumetsuItem extends ForgeSpawnEggItem {
    public static boolean spawn = false;

    public FumetsuItem(Properties props) {
        super(HyperEntities.FUMETSU, 0xFFFFFF, 0x000000, props);
    }

    @Override
    public int getColor(int pTintIndex) {
        if (pTintIndex == 0) {
            return super.getColor(pTintIndex);
        } else {
            TextColor color = Writes.gameOver("A").getSiblings().get(0).getStyle().getColor();
            return color != null ? color.getValue() : super.getColor(pTintIndex);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        spawn = true;
        InteractionResult result = super.useOn(pContext);
        spawn = false;
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        spawn = true;
        InteractionResultHolder<ItemStack> result = super.use(pLevel, pPlayer, pHand);
        spawn = false;
        return result;
    }
}
