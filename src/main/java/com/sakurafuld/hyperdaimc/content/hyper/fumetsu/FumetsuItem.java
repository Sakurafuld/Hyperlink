package com.sakurafuld.hyperdaimc.content.hyper.fumetsu;

import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class FumetsuItem extends ForgeSpawnEggItem {
    public FumetsuItem(Properties props) {
        super(HyperEntities.FUMETSU, 0xFFFFFF, 0x000000, props);
    }

    @Override
    public int getColor(int pTintIndex) {
        if (pTintIndex == 0)
            return super.getColor(pTintIndex);
        else
            return Writes.gameOver(0);
    }
}
