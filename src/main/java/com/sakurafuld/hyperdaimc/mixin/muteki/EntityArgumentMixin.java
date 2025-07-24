package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mixin(EntityArgument.class)
public abstract class EntityArgumentMixin {
    @Inject(method = "getEntity", at = @At("RETURN"), cancellable = true)
    private static void getEntityMuteki(CommandContext<CommandSourceStack> pContext, String pName, CallbackInfoReturnable<Entity> cir) throws CommandSyntaxException {
        if (HyperServerConfig.MUTEKI_SELECTOR.get()) {
            Entity found = pContext.getArgument(pName, EntitySelector.class).findEntities(pContext.getSource()).stream()
                    .filter(entity -> !(entity instanceof LivingEntity living && MutekiHandler.muteki(living)))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                LOG.debug("getEntityMuteki");
                throw MutekiHandler.ERROR_NOT_FOUND.create();
            } else {
                cir.setReturnValue(found);
            }
        }
    }

    @Inject(method = "getOptionalEntities", at = @At("RETURN"), cancellable = true)
    private static void getOptionalEntitiesMuteki(CommandContext<CommandSourceStack> pContext, String pName, CallbackInfoReturnable<Collection<? extends Entity>> cir) throws CommandSyntaxException {
        if (!cir.getReturnValue().isEmpty() && HyperServerConfig.MUTEKI_SELECTOR.get()) {
            Collection<Entity> found = cir.getReturnValue().stream()
                    .filter(entity -> !(entity instanceof LivingEntity living && MutekiHandler.muteki(living)))
                    .collect(Collectors.toList());

            if (found.isEmpty()) {
                LOG.debug("getOptionalEntitiesMuteki");
                throw MutekiHandler.ERROR_NOT_FOUND.create();
            } else {
                cir.setReturnValue(found);
            }
        }
    }

    @Inject(method = "getOptionalPlayers", at = @At("RETURN"), cancellable = true)
    private static void getOptionalPlayersMuteki(CommandContext<CommandSourceStack> pContext, String pName, CallbackInfoReturnable<Collection<ServerPlayer>> cir) throws CommandSyntaxException {
        if (!cir.getReturnValue().isEmpty() && HyperServerConfig.MUTEKI_SELECTOR.get()) {
            Collection<ServerPlayer> found = cir.getReturnValue().stream()
                    .filter(entity -> !MutekiHandler.muteki(entity))
                    .collect(Collectors.toList());

            if (found.isEmpty()) {
                LOG.debug("getOptionalPlayersMuteki");
                throw MutekiHandler.ERROR_NOT_FOUND.create();
            } else {
                cir.setReturnValue(found);
            }
        }
    }

    @Inject(method = "getPlayer", at = @At("RETURN"), cancellable = true)
    private static void getPlayerMuteki(CommandContext<CommandSourceStack> pContext, String pName, CallbackInfoReturnable<ServerPlayer> cir) throws CommandSyntaxException {
        if (HyperServerConfig.MUTEKI_SELECTOR.get()) {
            ServerPlayer found = pContext.getArgument(pName, EntitySelector.class).findPlayers(pContext.getSource()).stream()
                    .filter(entity -> !MutekiHandler.muteki(entity))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                LOG.debug("getPlayerMuteki");
                throw MutekiHandler.ERROR_NOT_FOUND.create();
            } else {
                cir.setReturnValue(found);
            }
        }
    }

    @Inject(method = "getPlayers", at = @At("RETURN"), cancellable = true)
    private static void getPlayersMuteki(CommandContext<CommandSourceStack> pContext, String pName, CallbackInfoReturnable<Collection<ServerPlayer>> cir) throws CommandSyntaxException {
        if (!cir.getReturnValue().isEmpty() && HyperServerConfig.MUTEKI_SELECTOR.get()) {
            Collection<ServerPlayer> found = cir.getReturnValue().stream()
                    .filter(entity -> !MutekiHandler.muteki(entity))
                    .collect(Collectors.toList());

            if (found.isEmpty()) {
                LOG.debug("getPlayersMuteki");
                throw MutekiHandler.ERROR_NOT_FOUND.create();
            } else {
                cir.setReturnValue(found);
            }
        }
    }
}
