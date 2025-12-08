package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxActor;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record ServerboundParadoxAction(Action action) {
    public static void encode(ServerboundParadoxAction msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static ServerboundParadoxAction decode(FriendlyByteBuf buf) {
        return new ServerboundParadoxAction(buf.readEnum(Action.class));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> this.action().go(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    public enum Action {
        SELECT(ParadoxActor::select),
        UNCHAIN(ParadoxActor::unselect),
        UNSELECT_AND_CHAIN(ParadoxActor::unselectAndChain),
        PICK(ParadoxActor::pick),
        UNPICK(ParadoxActor::unpick),
        PERFECT_KNOCKOUT(ParadoxActor::perfectKnockout, true),
        PERFECT_KNOCKOUT_CLUSTER(ParadoxActor::perfectKnockoutCluster);

        private final BiConsumer<ServerPlayer, ParadoxCapabilityPlayer> action;
        private final boolean fuzzy;

        Action(BiConsumer<ServerPlayer, ParadoxCapabilityPlayer> action, boolean fuzzy) {
            this.action = action;
            this.fuzzy = fuzzy;
        }

        Action(BiConsumer<ServerPlayer, ParadoxCapabilityPlayer> action) {
            this(action, false);
        }

        public void go(ServerPlayer player) {
            if (this.fuzzy ? ParadoxHandler.hasParadox(player) : player.getMainHandItem().is(HyperItems.PARADOX.get()))
                player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox ->
                        this.action.accept(player, paradox));
        }
    }
}
