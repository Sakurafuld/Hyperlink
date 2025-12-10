package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.mojang.datafixers.util.Either;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin {
    @Inject(method = "generate", at = @At("HEAD"))
    private void generateChronicle$HEAD(Executor pExectutor, ServerLevel pLevel, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, List<ChunkAccess> pCache, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
        ChronicleHandler.chunkGenerating = true;
    }

    @Inject(method = "generate", at = @At("RETURN"))
    private void generateChronicle$RETURN(Executor pExectutor, ServerLevel pLevel, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, List<ChunkAccess> pCache, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
        ChronicleHandler.chunkGenerating = false;
    }
}
