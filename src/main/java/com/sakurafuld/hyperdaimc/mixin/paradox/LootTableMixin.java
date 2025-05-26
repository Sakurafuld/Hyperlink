package com.sakurafuld.hyperdaimc.mixin.paradox;

import com.sakurafuld.hyperdaimc.api.mixin.ILootTableParadox;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LootTable.class)
public abstract class LootTableMixin implements ILootTableParadox {

    @Shadow
    @Final
    private List<LootPool> pools;

    @Override
    public boolean isNoDrop() {
        return this.pools.isEmpty();
    }
}
