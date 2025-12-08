package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.Objects;
import java.util.function.Supplier;

@Deprecated
public record VulnerableServerboundVRXOpenMenu(VRXMenu.Canvas canvas) {
    public static void encode(VulnerableServerboundVRXOpenMenu msg, FriendlyByteBuf buf) {
        msg.canvas.write(buf);
    }

    public static VulnerableServerboundVRXOpenMenu decode(FriendlyByteBuf buf) {
        return new VulnerableServerboundVRXOpenMenu(VRXMenu.Canvas.read(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!HyperCommonConfig.VRX_VULNERABILIZATION.get())
            return;

        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            Component name = this.canvas().supply(player.level(), block -> block.getBlockState().getBlock().getName(), Entity::getDisplayName);
//            if (this.canvas.block != null) {
//                BlockEntity block = this.canvas.getBlock(player.level());
//                if (block == null) return;
//                else name = block.getBlockState().getBlock().getName();
//            } else {
//                Entity entity = this.canvas.getEntity(player.level());
//                if (entity == null) return;
//                else name = entity.getDisplayName();
//            }

            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return name;
                }

                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    return new VRXMenu(pContainerId, pPlayerInventory, VulnerableServerboundVRXOpenMenu.this.canvas);
                }
            }, VulnerableServerboundVRXOpenMenu.this.canvas::write);
        });
        ctx.get().setPacketHandled(true);
    }
}
