package com.sakurafuld.hyperdaimc.network.vrx;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXCapability;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXHandler;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundVRXMyself(boolean open) {
    public static void encode(ServerboundVRXMyself msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.open);
    }

    public static ServerboundVRXMyself decode(FriendlyByteBuf buf) {
        return new ServerboundVRXMyself(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());

            // CreativeModeInventoryScreen.ItemPickerMenuの対応が、、.
            if (player.isCreative() || player.containerMenu.getCarried().is(HyperItems.VRX.get())) {
                if (this.open) {
                    Pair<List<Direction>, List<VRXType>> pair = VRXMenu.Canvas.getAvailables(player);
                    VRXMenu.Canvas canvas = VRXMenu.Canvas.entity(player.getId(), null, pair.getFirst(), pair.getSecond());
                    NetworkHooks.openScreen(player, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return player.getDisplayName();
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                            return new VRXMenu(pContainerId, pPlayerInventory, canvas);
                        }
                    }, canvas::write);

                    player.playNotifySound(HyperSounds.VRX_OPEN.get(), SoundSource.PLAYERS, 0.5f, 0.75f);
                } else {
                    player.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                        vrx.erase(player.getUUID());
                        vrx.sync2Client(player.getId(), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
                    });
                    VRXHandler.playSound(player.serverLevel(), player.position(), false);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
