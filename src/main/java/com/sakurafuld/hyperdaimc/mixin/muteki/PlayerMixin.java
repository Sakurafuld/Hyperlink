package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements ILivingEntityMuteki {
    @Unique
    private Boolean initialized = null;
    @Unique
    private Pair<Long, Boolean> last = null;

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$Player(DamageSource pDamageSource, CallbackInfo ci) {
        Player self = (Player) ((Object) this);

        if ((!Float.isFinite(self.getHealth()) || HyperServerConfig.MUTEKI_NOVEL.get() || !NovelHandler.novelized(self)) && MutekiHandler.muteki(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tickMuteki(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile, CallbackInfo ci) {
        this.initialized = true;
    }

    @Override
    public boolean muteki() {
        if (this.initialized != null && this.initialized) {
            Player self = (Player) (Object) this;
            if (this.last == null || this.last.getFirst() != self.getLevel().getGameTime()) {
                this.last = Pair.of(self.getLevel().getGameTime(), MutekiHandler.checkMuteki(self));
            }
            return this.last.getSecond();
        } else {
            return false;
        }
    }
}
