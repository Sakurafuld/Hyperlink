package com.sakurafuld.hyperdaimc.content.fumetsu;

import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.helper.Writes;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class FumetsuItem extends ForgeSpawnEggItem {
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
}
