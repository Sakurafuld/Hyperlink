package com.sakurafuld.hyperdaimc.network.desk;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskMenu;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundDeskLockRecipe {
    private final int id;
    private final Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock;

    public ServerboundDeskLockRecipe(int id, Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock) {
        this.id = id;
        this.lock = lock;
    }

    public static void encode(ServerboundDeskLockRecipe msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeVarInt(msg.lock.size());
        msg.lock.forEach((item, set) -> {
            buf.writeVarInt(Item.getId(item));
            buf.writeVarInt(set.size());
            set.forEach(buf::writeVarInt);
        });
    }

    public static ServerboundDeskLockRecipe decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();

        Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock = new Object2ObjectOpenHashMap<>();
        int lockSize = buf.readVarInt();
        for (int index = 0; index < lockSize; index++) {
            Item item = Item.byId(buf.readVarInt());

            IntAVLTreeSet set = new IntAVLTreeSet();
            int setSize = buf.readVarInt();
            for (int slot = 0; slot < setSize; slot++) {
                set.add(buf.readVarInt());
            }

            lock.put(item, set);
        }
        return new ServerboundDeskLockRecipe(id, lock);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender().containerMenu instanceof DeskMenu menu && menu.containerId == this.id) {
                menu.access.execute(((level, pos) -> level.getBlockEntity(pos, HyperBlockEntities.DESK.get()).ifPresent(desk ->
                        desk.lockRecipe(this.lock))));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
