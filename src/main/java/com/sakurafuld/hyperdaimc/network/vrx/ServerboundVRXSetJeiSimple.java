package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public record ServerboundVRXSetJeiSimple(int id, int index, ItemStack stack) {
    public static void encode(ServerboundVRXSetJeiSimple msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeVarInt(msg.index);
        buf.writeItemStack(msg.stack, false);
    }

    public static ServerboundVRXSetJeiSimple decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        int index = buf.readVarInt();
        return new ServerboundVRXSetJeiSimple(id, index, buf.readItem());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender().containerMenu instanceof VRXMenu menu && menu.containerId == this.id) {
                if (HyperCommonConfig.VRX_SEAL_HYPERLINK.get() && !HyperCommonConfig.VRX_VULNERABILIZATION.get() && ForgeRegistries.ITEMS.getKey(this.stack.getItem()).getNamespace().equals(HYPERDAIMC))
                    return;

                menu.getSlot(this.index).set(this.stack);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
