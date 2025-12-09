package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxHandler;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatParticle;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatParticleOptions;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundPerfectKnockedoutParticles(Long2IntOpenHashMap map) {
    public static void encode(ClientboundPerfectKnockedoutParticles msg, FriendlyByteBuf buf) {
        buf.writeMap(msg.map, FriendlyByteBuf::writeVarLong, FriendlyByteBuf::writeVarInt);
    }

    public static ClientboundPerfectKnockedoutParticles decode(FriendlyByteBuf buf) {
        return new ClientboundPerfectKnockedoutParticles(buf.readMap(Long2IntOpenHashMap::new, FriendlyByteBuf::readVarLong, FriendlyByteBuf::readVarInt));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        if (!ParadoxHandler.RenderingLevel.TERRAIN.check())
            return;
        boolean gashat = ParadoxHandler.RenderingLevel.ALL.check();
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = Objects.requireNonNull(mc.level);
        for (Long2IntMap.Entry entry : this.map().long2IntEntrySet()) {
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (!level.isLoaded(pos))
                continue;
            BlockState state = Block.stateById(entry.getIntValue());
            ParticleEngine engine = mc.particleEngine;
            if (!state.isAir() && !IClientBlockExtensions.of(state).addDestroyEffects(state, level, pos, engine)) {
                VoxelShape shape = state.getBlock() instanceof LiquidBlock ? Shapes.block() : state.getShape(level, pos);
                shape.forAllBoxes((startX, startY, startZ, endX, endY, endZ) -> {
                    double minX = Math.min(1, endX - startX);
                    double minY = Math.min(1, endY - startY);
                    double minZ = Math.min(1, endZ - startZ);
                    double maxX = Math.max(2, Mth.ceil(minX / 0.25));
                    double maxY = Math.max(2, Mth.ceil(minY / 0.25));
                    double maxZ = Math.max(2, Mth.ceil(minZ / 0.25));

                    for (int x = 0; x < maxX; ++x) {
                        for (int y = 0; y < maxY; ++y) {
                            for (int z = 0; z < maxZ; ++z) {
                                double dx = (x + 0.5) / maxX;
                                double dy = (y + 0.5) / maxY;
                                double dz = (z + 0.5) / maxZ;
                                double offsetX = dx * minX + startX;
                                double offsetY = dy * minY + startY;
                                double offsetZ = dz * minZ + startZ;
                                Particle particle;
                                if (gashat && level.getRandom().nextBoolean()) {
                                    GashatParticleOptions options = GashatParticleOptions.drop(level.getRandom()::nextFloat, 1);
                                    particle = new GashatParticle(options, level, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, dx - 0.5, dy - 0.5, dz - 0.5);
                                } else
                                    particle = new TerrainParticle(level, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, dx - 0.5, dy - 0.5, dz - 0.5, state, pos).updateSprite(state, pos);
                                engine.add(particle);
                            }
                        }
                    }
                });
            }
        }
    }
}
