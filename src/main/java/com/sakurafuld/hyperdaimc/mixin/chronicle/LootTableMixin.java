package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void getRandomItemsChronicle(LootContext pContext, Consumer<ItemStack> pStacksOut, CallbackInfo ci) {
        Vec3 vec = pContext.getParamOrNull(LootContextParams.ORIGIN);
        if (pContext.getParamOrNull(LootContextParams.BLOCK_STATE) != null && vec != null) {
            BlockPos pos = BlockPos.containing(vec);

            if (ChronicleHandler.isPaused(pContext.getLevel(), pos, pContext.getParamOrNull(LootContextParams.THIS_ENTITY))) {
                ci.cancel();
            }
        }
    }
}
