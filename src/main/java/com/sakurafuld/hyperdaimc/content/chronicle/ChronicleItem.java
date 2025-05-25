package com.sakurafuld.hyperdaimc.content.chronicle;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ChronicleItem extends AbstractGashatItem {

    public ChronicleItem(String name, Properties pProperties) {
        super(name, pProperties, 0x00FFAA, HyperServerConfig.ENABLE_CHRONICLE);
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!this.enabled.get()) {
            return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
        }
        return !pPlayer.isCreative();
    }
}
