package com.sakurafuld.hyperdaimc.network.vrx;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.content.vrx.VRXMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class ServerboundVRXOpenMenu {
    private final Pair<BlockPos, Integer> provider;
    private final Pair<Pair<BlockPos, Integer>, Direction> pair;

    public ServerboundVRXOpenMenu(Pair<Pair<BlockPos, Integer>, Direction> pair) {
        this.pair = pair;
        this.provider = pair.getFirst();
    }

    public static void encode(ServerboundVRXOpenMenu msg, FriendlyByteBuf buf) {
        VRXMenu.parse(buf, msg.pair);
    }

    public static ServerboundVRXOpenMenu decode(FriendlyByteBuf buf) {
        return new ServerboundVRXOpenMenu(VRXMenu.parse(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Component name;
            if (this.provider.getFirst() != null) {
                BlockState state = player.level().getBlockState(this.provider.getFirst());
                if (!state.hasBlockEntity()) {
                    return;
                } else {
                    name = state.getBlock().getName();
                }
            } else {
                Entity entity = player.level().getEntity(this.provider.getSecond());
                if (entity == null) {
                    return;
                } else {
                    name = entity.getDisplayName();
                }
            }
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return name;
                }

                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    return new VRXMenu(pContainerId, pPlayerInventory, ServerboundVRXOpenMenu.this.pair);
                }
            }, buf -> VRXMenu.parse(buf, ServerboundVRXOpenMenu.this.pair));

        });
        ctx.get().setPacketHandled(true);
    }
}
