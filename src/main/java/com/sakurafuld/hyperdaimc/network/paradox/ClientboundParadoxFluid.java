package com.sakurafuld.hyperdaimc.network.paradox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundParadoxFluid {
    private final BlockPos pos;
    private final BlockState state;

    public ClientboundParadoxFluid(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public static void encode(ClientboundParadoxFluid msg, FriendlyByteBuf buf) {
        LOG.info("encodeUpdateParadox");
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(Block.getId(msg.state));
    }

    public static ClientboundParadoxFluid decode(FriendlyByteBuf buf) {
        LOG.info("decodeUpdateParadox");
        return new ClientboundParadoxFluid(buf.readBlockPos(), Block.stateById(buf.readVarInt()));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        LOG.info("handleUpdateParadox");
        this.destroyParticle(this.pos, this.state);
    }

    @OnlyIn(Dist.CLIENT)
    private void destroyParticle(BlockPos pos, BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        ParticleEngine engine = mc.particleEngine;
        if (!state.isAir() && !RenderProperties.get(state).addDestroyEffects(state, level, pos, engine)) {
            Shapes.block().forAllBoxes((startX, startY, startZ, endX, endY, endZ) -> {
                double minX = Math.min(1, endX - startX);
                double minY = Math.min(1, endY - startY);
                double minZ = Math.min(1, endZ - startZ);
                int maxX = Math.max(2, Mth.ceil(minX / 0.25));
                int maxY = Math.max(2, Mth.ceil(minY / 0.25));
                int maxZ = Math.max(2, Mth.ceil(minZ / 0.25));

                for(int x = 0; x < maxX; ++x) {
                    for(int y = 0; y < maxY; ++y) {
                        for(int z = 0; z < maxZ; ++z) {
                            double d4 = ((double)x + 0.5) / (double)maxX;
                            double d5 = ((double)y + 0.5) / (double)maxY;
                            double d6 = ((double)z + 0.5) / (double)maxZ;
                            double d7 = d4 * minX + startX;
                            double d8 = d5 * minY + startY;
                            double d9 = d6 * minZ + startZ;
                            engine.add(new TerrainParticle(level, (double)pos.getX() + d7, (double)pos.getY() + d8, (double)pos.getZ() + d9, d4 - 0.5, d5 - 0.5, d6 - 0.5, state, pos).updateSprite(state, pos));
                        }
                    }
                }
            });
        }
    }
}
