package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "useOn", at = @At("HEAD"))
    private void useOnChronicle$HEAD(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPlaceContext ctx = new BlockPlaceContext(pContext);
        if (!HyperCommonConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(ctx.getLevel(), ctx.getClickedPos(), null) && !ChronicleHandler.isPaused(ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer()))
            ParadoxHandler.gashaconPlayer = ctx.getPlayer();
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void useOnChronicle$RETURN(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPlaceContext ctx = new BlockPlaceContext(pContext);
        if (!HyperCommonConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(ctx.getLevel(), ctx.getClickedPos(), null) && !ChronicleHandler.isPaused(ctx.getLevel(), ctx.getClickedPos(), ctx.getPlayer()))
            ParadoxHandler.gashaconPlayer = null;
    }
}
