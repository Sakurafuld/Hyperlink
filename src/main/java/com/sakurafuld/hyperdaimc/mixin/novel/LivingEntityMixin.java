package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.novel.NovelDamageSource;
import com.sakurafuld.hyperdaimc.content.novel.NovelItem;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import static com.sakurafuld.hyperdaimc.Deets.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityNovel {
    @Shadow @Final private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Override
    public void novelKill(LivingEntity pEntity){
        LOG.debug("{}-killNovel", side());
        LivingEntity self = (LivingEntity) ((Object) this);
        MutekiHandler.setNovelized(self);
        NovelDamageSource damage = new NovelDamageSource(pEntity);
        self.getCombatTracker().recordDamage(damage, self.getHealth(), 0);
        this.novelSetHealth();
        required(LogicalSide.SERVER).run(()->{
            if(self.isDeadOrDying()){
                self.die(damage);
            }
        });
        MutekiHandler.setNovelized(null);
    }
    @Override
    public void novelHeartAttack() {
        ((LivingEntity) ((Object) this)).getEntityData().set(DATA_HEALTH_ID, 0f);
    }
    @Unique
    private void novelSetHealth(){
        LivingEntity self = (LivingEntity) ((Object) this);
        if(!CuriosApi.getCuriosHelper().findCurios(self, ModItems.MUTEKI.get()).isEmpty()){
            if(MutekiHandler.getNovelized() != null && MutekiHandler.getNovelized().equals(self)){
                self.getEntityData().set(DATA_HEALTH_ID, self.getHealth() - 1);
                self.hurtDuration = 10;
                self.hurtTime = self.hurtDuration;
            }
        }else{
            self.getEntityData().set(DATA_HEALTH_ID, 0f);
        }
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;tickDeath()V", shift = At.Shift.AFTER))
    private void baseTickNovel(CallbackInfo ci){
        LivingEntity self = (LivingEntity) ((Object) this);
        ((NovelItem) ModItems.NOVEL.get()).tryKillRobit((LivingEntity) ((Object) this));
    }
}
