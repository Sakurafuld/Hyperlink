package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILevelChunkSectionChronicle;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {
    @Shadow
    @Final
    protected LevelChunkSection[] sections;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void contractorChronicle(ChunkPos pChunkPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, long pInhabitedTime, LevelChunkSection[] pSections, BlendingData pBlendingData, CallbackInfo ci) {
        for (LevelChunkSection section : this.sections) {
            ((ILevelChunkSectionChronicle) section).hyperdaimc$setChunk((ChunkAccess) ((Object) this));
        }
    }
}
