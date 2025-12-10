package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxHandler;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatParticle;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
@OnlyIn(Dist.CLIENT)
public abstract class ParticleEngineMixin {
    @Shadow
    protected ClientLevel level;

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    public abstract void add(Particle pEffect);

    @Inject(method = "addBlockHitEffects", at = @At("HEAD"), cancellable = true, remap = false)
    private void addBlockHitEffectsChronicle(BlockPos pos, BlockHitResult target, CallbackInfo ci) {
        if (!HyperCommonConfig.CHRONICLE_SHOW_PROTECTION.get())
            return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (!ParadoxHandler.againstChronicle(player) && ChronicleHandler.isPaused(this.level, pos, player)) {
            ci.cancel();
            BlockState state = this.level.getBlockState(pos);
            Direction direction = target.getDirection();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            AABB aabb = state.getShape(this.level, pos).bounds();
            double dx = x + this.random.nextDouble() * (aabb.maxX - aabb.minX - 0.2) + 0.1 + aabb.minX;
            double dy = y + this.random.nextDouble() * (aabb.maxY - aabb.minY - 0.2) + 0.1 + aabb.minY;
            double dz = z + this.random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2) + 0.1 + aabb.minZ;
            switch (direction) {
                case DOWN -> dy = y + aabb.minY - 0.1;
                case UP -> dy = y + aabb.maxY + 0.1;
                case NORTH -> dz = z + aabb.minZ - 0.1;
                case SOUTH -> dz = z + aabb.maxZ + 0.1;
                case WEST -> dx = x + aabb.minX - 0.1;
                case EAST -> dx = x + aabb.maxX + 0.1;
            }

            GashatParticleOptions options = GashatParticleOptions.drop(this.random::nextFloat, 1);
            GashatParticle particle = new GashatParticle(options, this.level, dx, dy, dz, direction.getStepX() * (0.5 + this.random.nextDouble() * 2), direction.getStepY() * (0.5 + this.random.nextDouble() * 2), direction.getStepZ() * (0.5 + this.random.nextDouble() * 2));
            this.add(particle);
        }
    }
}
