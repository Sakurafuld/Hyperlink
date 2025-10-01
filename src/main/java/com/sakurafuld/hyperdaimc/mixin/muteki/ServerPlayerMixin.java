package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.mojang.authlib.GameProfile;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    protected abstract void tellNeutralMobsThatIDied();

    public ServerPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$Player(DamageSource pDamageSource, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) ((Object) this);

        if (NovelHandler.novelized(self)) {
            ci.cancel();
            Deets.LOG.debug("ServerPlayerDiesNovelized:{}", Arrays.stream(Thread.currentThread().getStackTrace())
                    .skip(3)
                    .limit(10)
                    .map(e -> "\n" + e.getClassName() + "." + e.getMethodName() + "@" + e.getLineNumber())
                    .toList());
            this.gameEvent(GameEvent.ENTITY_DIE);
            ForgeHooks.onLivingDeath(this, pDamageSource);
            boolean showDeathMessage = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
            if (showDeathMessage) {
                Component component = this.getCombatTracker().getDeathMessage();
                this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), component), PacketSendListener.exceptionallySend(() -> {
                    String s = component.getString(256);
                    Component component1 = Component.translatable("death.attack.message_too_long", Component.literal(s).withStyle(ChatFormatting.YELLOW));
                    Component component2 = Component.translatable("death.attack.even_more_magic", this.getDisplayName()).withStyle((p_143420_) -> p_143420_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1)));
                    return new ClientboundPlayerCombatKillPacket(this.getId(), component2);
                }));
                Team team = this.getTeam();
                if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                    if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                        this.server.getPlayerList().broadcastSystemToTeam(this, component);
                    } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                        this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, component);
                    }
                } else {
                    this.server.getPlayerList().broadcastSystemMessage(component, false);
                }
                Deets.LOG.debug("SPDiesNovelized:{}", component.getString());
            } else {
                this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
            }

            this.removeEntitiesOnShoulder();
            if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
                this.tellNeutralMobsThatIDied();
            }

            if (!this.isSpectator()) {
                this.dropAllDeathLoot(pDamageSource);
            }

            this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this.getScoreboardName(), Score::increment);
            LivingEntity livingentity = this.getKillCredit();
            if (livingentity != null) {
                this.awardStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
                livingentity.awardKillScore(this, this.deathScore, pDamageSource);
                this.createWitherRose(livingentity);
            }

            this.level().broadcastEntityEvent(this, EntityEvent.DEATH);
            this.awardStat(Stats.DEATHS);
            this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
            this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            this.clearFire();
            this.setTicksFrozen(0);
            this.setSharedFlagOnFire(false);
            this.getCombatTracker().recheckStatus();
            this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
        } else if (MutekiHandler.muteki(self)) {
            ci.cancel();
            Deets.LOG.debug("ServerPlayerCancelDieMuteki");
        }
    }
}
