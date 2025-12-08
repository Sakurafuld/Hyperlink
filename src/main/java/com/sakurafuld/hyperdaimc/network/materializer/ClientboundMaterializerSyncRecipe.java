package com.sakurafuld.hyperdaimc.network.materializer;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundMaterializerSyncRecipe(BlockPos pos, List<ItemStack> processRecipe) {
    public static void encode(ClientboundMaterializerSyncRecipe msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeCollection(msg.processRecipe, (buf1, stack) -> buf1.writeItemStack(stack, false));
    }

    public static ClientboundMaterializerSyncRecipe decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        List<ItemStack> processRecipe = buf.readCollection(size -> Lists.newArrayList(), FriendlyByteBuf::readItem);
        return new ClientboundMaterializerSyncRecipe(pos, processRecipe);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Objects.requireNonNull(Minecraft.getInstance().level).getBlockEntity(this.pos, HyperBlockEntities.MATERIALIZER.get())
                .ifPresent(materializer ->
                        materializer.updateRecipe(this.processRecipe));
    }
}
