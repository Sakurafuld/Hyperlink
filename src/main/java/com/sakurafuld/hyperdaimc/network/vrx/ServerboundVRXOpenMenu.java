package com.sakurafuld.hyperdaimc.network.vrx;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXCapability;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class ServerboundVRXOpenMenu {
    private final boolean block;

    public ServerboundVRXOpenMenu(boolean block) {
        this.block = block;
    }

    public static void encode(ServerboundVRXOpenMenu msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.block);
    }

    public static ServerboundVRXOpenMenu decode(FriendlyByteBuf buf) {
        return new ServerboundVRXOpenMenu(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            double reach = Math.max(player.getBlockReach(), player.getEntityReach());
            Pair<Pair<BlockPos, Integer>, Direction> pair = null;
            Component name = null;

            if (this.block) {
                if (player.pick(reach, 1, false) instanceof BlockHitResult hit && hit.getType() != HitResult.Type.MISS) {
                    BlockState state = player.level().getBlockState(hit.getBlockPos());
                    if (state.hasBlockEntity()) {
                        pair = Pair.of(Pair.of(hit.getBlockPos(), null), hit.getDirection());
                        name = state.getBlock().getName();
                    }
                }
            } else {
                Vec3 start = player.getEyePosition();
                Vec3 vector = player.getViewVector(1).scale(reach);
                Vec3 end = start.add(vector);
                AABB aabb = player.getBoundingBox().expandTowards(vector).inflate(1);
                EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, aabb, entity -> !entity.isSpectator() && entity.isPickable(), reach * reach);
                if (hit != null && hit.getType() != HitResult.Type.MISS && (HyperCommonConfig.VRX_PLAYER.get() || !(hit.getEntity() instanceof Player)) && hit.getEntity().getCapability(VRXCapability.TOKEN).isPresent()) {
                    pair = Pair.of(Pair.of(null, hit.getEntity().getId()), null);
                    name = hit.getEntity().getDisplayName();
                }
            }

            if (pair != null) {

                Component finalName = name;
                Pair<Pair<BlockPos, Integer>, Direction> finalPair = pair;
                NetworkHooks.openScreen(player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return finalName;
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                        return new VRXMenu(pContainerId, pPlayerInventory, finalPair);
                    }
                }, buf -> VRXMenu.parse(buf, finalPair));

                player.playNotifySound(HyperSounds.VRX_OPEN.get(), SoundSource.PLAYERS, 0.5f, 0.75f);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
