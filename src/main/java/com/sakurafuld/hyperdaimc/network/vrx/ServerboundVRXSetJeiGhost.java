package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOne;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class ServerboundVRXSetJeiGhost {
    private final int id;
    private final int index;
    @Nullable
    private final VRXOne one;
    @Nullable
    private final ItemStack stack;

    public ServerboundVRXSetJeiGhost(int id, int index, VRXOne one) {
        this.id = id;
        this.index = index;
        this.one = one;
        this.stack = null;
    }

    public ServerboundVRXSetJeiGhost(int id, int index, ItemStack stack) {
        this.id = id;
        this.index = index;
        this.one = null;
        this.stack = stack;
    }

    public static void encode(ServerboundVRXSetJeiGhost msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeVarInt(msg.index);
        if (msg.one != null) {
            buf.writeBoolean(true);
            buf.writeNbt(msg.one.serialize());
        } else {
            buf.writeBoolean(false);
            buf.writeItemStack(msg.stack, false);
        }
    }

    public static ServerboundVRXSetJeiGhost decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        int index = buf.readVarInt();

        if (buf.readBoolean()) {
            CompoundTag tag = buf.readNbt();
            VRXOne.Type type = VRXOne.Type.of(tag.getString("Type"));
            VRXOne one = type.load(tag.getCompound("Data"));
            return new ServerboundVRXSetJeiGhost(id, index, one);
        } else {
            return new ServerboundVRXSetJeiGhost(id, index, buf.readItem());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender().containerMenu instanceof VRXMenu menu && menu.containerId == this.id) {
                if (HyperCommonConfig.VRX_SEAL_HYPERLINK.get() && !HyperCommonConfig.VRX_VULNERABILIZATION.get()) {
                    ItemStack stack = null;
                    if (this.one instanceof VRXOne.Item item) {
                        stack = item.getItemStack();
                    } else if (this.stack != null) {
                        stack = this.stack;
                    }

                    if (stack != null && ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().equals(HYPERDAIMC)) {
                        return;
                    }
                }
                Slot slot = menu.getSlot(this.index);
                if (slot instanceof VRXSlot vrxSlot && this.one != null) {
                    vrxSlot.setOne(this.one);
                } else if (this.stack != null) {
                    slot.set(this.stack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
