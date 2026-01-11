package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IMetapotentFlashfurNovel;
import flashfur.omnimobs.entities.anticheat.AccessChecker;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfur;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurEntity;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurLevel;
import net.minecraft.server.TickTask;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MetapotentFlashfur.class)
public abstract class MetapotentFlashfurMixin implements IMetapotentFlashfurNovel {
    @Shadow(remap = false)
    private MetapotentFlashfurEntity metapotentFlashfurProxy;

    @Unique
    private int time = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void tickNovel(CallbackInfo ci) {
        if (this.metapotentFlashfurProxy != null && NovelHandler.novelized(this.metapotentFlashfurProxy)) {
            if (++this.time >= 20)
                LogicalSidedProvider.WORKQUEUE.get(EffectiveSide.get()).tell(new TickTask(0, this::remove));
            ci.cancel();
        }
    }

    @Override
    public MetapotentFlashfurEntity hyperdaimc$getOriginal() {
        return this.metapotentFlashfurProxy;
    }

    @Override
    public int hyperdaimc$getTime() {
        return this.time;
    }

    @Unique
    private void remove() {
        AccessChecker.performPrivilegedAction(() -> MetapotentFlashfurLevel.remove((MetapotentFlashfur) (Object) this));
    }
}
