package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.mixin.IMetapotentFlashfurNovel;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfur;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurEntity;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurLevel;
import net.minecraft.server.TickTask;
import net.minecraftforge.common.util.LogicalSidedProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sakurafuld.hyperdaimc.helper.Deets.side;

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
            if (++this.time >= 20) {
                LogicalSidedProvider.WORKQUEUE.get(side()).tell(new TickTask(0, () -> MetapotentFlashfurLevel.remove((MetapotentFlashfur) ((Object) this))));
            }
            ci.cancel();
        }
    }

    @Override
    public MetapotentFlashfurEntity getOriginal() {
        return this.metapotentFlashfurProxy;
    }

    @Override
    public int getTime() {
        return this.time;
    }
}
