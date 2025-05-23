package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.content.HyperSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

public class ServerboundChronicleSound {
    private final Vec3 position;
    private final boolean restart;

    public ServerboundChronicleSound(Vec3 position, boolean restart) {
        this.position = position;
        this.restart = restart;
    }

    public static void encode(ServerboundChronicleSound msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.position.x());
        buf.writeDouble(msg.position.y());
        buf.writeDouble(msg.position.z());
        buf.writeBoolean(msg.restart);
    }

    public static ServerboundChronicleSound decode(FriendlyByteBuf buf) {
        return new ServerboundChronicleSound(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOG.debug("handleChronicleSound");
            if (this.restart) {
                ctx.get().getSender().getLevel().playSound(null, this.position.x(), this.position.y(), this.position.z(), HyperSounds.CHRONICLE_RESTART.get(), SoundSource.PLAYERS, 1, 1);
            } else {
                ctx.get().getSender().getLevel().playSound(null, this.position.x(), this.position.y(), this.position.z(), HyperSounds.CHRONICLE_PAUSE.get(), SoundSource.PLAYERS, 1, 1);

            }
        });
        ctx.get().setPacketHandled(true);
    }
}
