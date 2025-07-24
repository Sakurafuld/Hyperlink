package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.mojang.authlib.GameProfile;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow
    protected abstract void tellNeutralMobsThatIDied();

    public ServerPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$Player(DamageSource pDamageSource, CallbackInfo ci) {
        Deets.LOG.debug("serverPlayerDied!!");
        ServerPlayer self = (ServerPlayer) ((Object) this);

        if ((!Float.isFinite(self.getHealth()) || !NovelHandler.novelized(self)) && MutekiHandler.muteki(self)) {
            ci.cancel();
        } else if (NovelHandler.novelized(self)) {
            ci.cancel();
            Deets.LOG.debug("serverPlayerDiedByNovel!!");
            ForgeHooks.onLivingDeath(self, pDamageSource);

            boolean flag = self.getLevel().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
            if (flag) {
                Deets.LOG.debug("serverPlayerDiedByNovelShowMessage!!");
                Component component = self.getCombatTracker().getDeathMessage();
                self.connection.send(new ClientboundPlayerCombatKillPacket(self.getCombatTracker(), component), (p_9142_) -> {
                    if (!p_9142_.isSuccess()) {
                        Deets.LOG.debug("serverPlayerDiedByNovelFail!!");
                        String s = component.getString(256);
                        Component component1 = new TranslatableComponent("death.attack.message_too_long", (new TextComponent(s)).withStyle(ChatFormatting.YELLOW));
                        Component component2 = (new TranslatableComponent("death.attack.even_more_magic", self.getDisplayName())).withStyle((p_143420_) -> p_143420_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1)));
                        self.connection.send(new ClientboundPlayerCombatKillPacket(self.getCombatTracker(), component2));
                    }

                });
                Team team = self.getTeam();
                if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                    if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                        self.server.getPlayerList().broadcastToTeam(self, component);
                    } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                        self.server.getPlayerList().broadcastToAllExceptTeam(self, component);
                    }
                } else {
                    self.server.getPlayerList().broadcastMessage(component, ChatType.SYSTEM, Util.NIL_UUID);
                }
            } else {
                Deets.LOG.debug("serverPlayerDiedByNovelNoMessage!!");
                self.connection.send(new ClientboundPlayerCombatKillPacket(this.getCombatTracker(), TextComponent.EMPTY));
            }

            this.removeEntitiesOnShoulder();
            if (self.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
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

            this.level.broadcastEntityEvent(this, (byte) 3);
            this.awardStat(Stats.DEATHS);
            this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
            this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            this.clearFire();
            this.setTicksFrozen(0);
            this.setSharedFlagOnFire(false);
            this.getCombatTracker().recheckStatus();
            Deets.LOG.debug("serverPlayerDiedByNovelEnd!!");
        }
    }
}
