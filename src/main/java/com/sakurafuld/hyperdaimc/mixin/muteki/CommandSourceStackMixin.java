package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {
    // ない方がいいかも.
//    @Inject(method = "getEntity", at = @At("RETURN"), cancellable = true)
//    private void getEntityMuteki(CallbackInfoReturnable<Entity> cir) {
//        if (!MutekiHandler.specialGameModeSwitch && HyperServerConfig.MUTEKI_SELECTOR.get() && cir.getReturnValue() instanceof LivingEntity entity && MutekiHandler.muteki(entity)) {
//            cir.setReturnValue(null);
//        }
//    }

    @Inject(method = "getEntityOrException", at = @At("RETURN"))
    private void getEntityOrExceptionMuteki(CallbackInfoReturnable<Entity> cir) throws CommandSyntaxException {
        if (HyperServerConfig.MUTEKI_SELECTOR.get() && cir.getReturnValue() instanceof LivingEntity entity && MutekiHandler.muteki(entity)) {
            throw MutekiHandler.ERROR_REQUIRE.create();
        }
    }

    @Inject(method = "getPlayerOrException", at = @At("RETURN"))
    private void getPlayerOrExceptionMuteki(CallbackInfoReturnable<ServerPlayer> cir) throws CommandSyntaxException {
        if (!MutekiHandler.specialGameModeSwitch && HyperServerConfig.MUTEKI_SELECTOR.get() && MutekiHandler.muteki(cir.getReturnValue())) {
            throw MutekiHandler.ERROR_REQUIRE.create();
        }
    }
}
