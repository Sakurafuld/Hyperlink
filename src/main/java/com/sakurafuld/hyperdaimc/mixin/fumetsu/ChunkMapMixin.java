package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Objects;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Unique
    private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap2 = new Int2ObjectOpenHashMap<>();
    @Shadow
    @Final
    ServerLevel level;

    @Shadow
    public abstract DistanceManager getDistanceManager();

    @Inject(method = "move", at = @At("HEAD"))
    private void moveFumetsu(ServerPlayer pPlayer, CallbackInfo ci) {
        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap2.values()) {
            chunkmap$trackedentity.updatePlayer(pPlayer);
        }
    }

    @Inject(locals = LocalCapture.CAPTURE_FAILSOFT, method = "addEntity", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    private void addEntityFumetsu$0(Entity pEntity, CallbackInfo ci, EntityType<?> entitytype, int i, int j, ChunkMap.TrackedEntity chunkmap$trackedentity) {
        if (pEntity instanceof IFumetsu) {
//            Deets.LOG.debug("addEntityFumetsu");
            ci.cancel();
            if (this.entityMap2.containsKey(pEntity.getId())) {
                throw Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
            } else {
                this.entityMap2.put(pEntity.getId(), chunkmap$trackedentity);
                chunkmap$trackedentity.updatePlayers(this.level.players());
            }
        }
    }

    @Inject(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;updatePlayerStatus(Lnet/minecraft/server/level/ServerPlayer;Z)V", shift = At.Shift.AFTER))
    private void addEntityFumetsu$1(Entity pEntity, CallbackInfo ci) {
        for (ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap2.values()) {
            chunkmap$trackedentity1.updatePlayer((ServerPlayer) pEntity);
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;updatePlayerStatus(Lnet/minecraft/server/level/ServerPlayer;Z)V", shift = At.Shift.AFTER))
    private void removeEntityFumetsu$0(Entity pEntity, CallbackInfo ci) {
        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap2.values()) {
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized(chunkmap$trackedentity.entity)) {
                chunkmap$trackedentity.removePlayer((ServerPlayer) pEntity);
            }
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;remove(I)Ljava/lang/Object;"))
    private void removeEntityFumetsu$1(Entity pEntity, CallbackInfo ci) {
        ChunkMap.TrackedEntity trackedEntity = this.entityMap2.get(pEntity.getId());
        if (trackedEntity != null) {
            if (FumetsuHandler.specialRemove.get() || ((IEntityNovel) trackedEntity.entity).hyperdaimc$isNovelized()) {
//                Deets.LOG.debug("removeEntityFumetsu");
                this.entityMap2.remove(pEntity.getId());
                trackedEntity.broadcastRemoved();
            }
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void tickFumetsu$0(CallbackInfo ci) {
        List<ServerPlayer> list = Lists.newArrayList();
        List<ServerPlayer> list1 = this.level.players();

        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap2.values()) {
            SectionPos sectionpos = chunkmap$trackedentity.lastSectionPos;
            SectionPos sectionpos1 = SectionPos.of(chunkmap$trackedentity.entity);
            boolean flag = !Objects.equals(sectionpos, sectionpos1);
            if (flag) {
                chunkmap$trackedentity.updatePlayers(list1);
                Entity entity = chunkmap$trackedentity.entity;
                if (entity instanceof ServerPlayer) {
                    list.add((ServerPlayer) entity);
                }

                chunkmap$trackedentity.lastSectionPos = sectionpos1;
            }

            if (flag || this.getDistanceManager().inEntityTickingRange(sectionpos1.chunk().toLong())) {
                chunkmap$trackedentity.serverEntity.sendChanges();
            }
        }

        if (!list.isEmpty()) {
            for (ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap2.values()) {
                chunkmap$trackedentity1.updatePlayers(list);
            }
        }
    }

    @Inject(method = "broadcast", at = @At("HEAD"))
    private void broadcastFumetsu(Entity pEntity, Packet<?> pPacket, CallbackInfo ci) {
        ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap2.get(pEntity.getId());
        if (chunkmap$trackedentity != null) {
            chunkmap$trackedentity.broadcast(pPacket);
        }
    }

    @Inject(method = "broadcastAndSend", at = @At("HEAD"))
    private void broadcastAndSend(Entity pEntity, Packet<?> pPacket, CallbackInfo ci) {
        ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap2.get(pEntity.getId());
        if (chunkmap$trackedentity != null) {
            chunkmap$trackedentity.broadcastAndSend(pPacket);
        }
    }

    @Inject(method = "playerLoadedChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/DebugPackets;sendPoiPacketsForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ChunkPos;)V", shift = At.Shift.AFTER))
    private void playerLoadedChunkFumetsu(ServerPlayer pPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, LevelChunk pChunk, CallbackInfo ci) {
        List<Entity> list = Lists.newArrayList();
        List<Entity> list1 = Lists.newArrayList();

        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap2.values()) {
            Entity entity = chunkmap$trackedentity.entity;
            if (entity != pPlayer && entity.chunkPosition().equals(pChunk.getPos())) {
                chunkmap$trackedentity.updatePlayer(pPlayer);
                if (entity instanceof Mob && ((Mob) entity).getLeashHolder() != null) {
                    list.add(entity);
                }

                if (!entity.getPassengers().isEmpty()) {
                    list1.add(entity);
                }
            }
        }

        if (!list.isEmpty()) {
            for (Entity entity1 : list) {
                pPlayer.connection.send(new ClientboundSetEntityLinkPacket(entity1, ((Mob) entity1).getLeashHolder()));
            }
        }

        if (!list1.isEmpty()) {
            for (Entity entity2 : list1) {
                pPlayer.connection.send(new ClientboundSetPassengersPacket(entity2));
            }
        }
    }
}
