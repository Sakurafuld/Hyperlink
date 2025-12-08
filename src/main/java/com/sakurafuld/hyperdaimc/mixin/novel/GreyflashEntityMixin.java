package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import net.mcreator.dragionnsstuff.entity.GreyflashEntity;
import net.mcreator.dragionnsstuff.procedures.GreyflashEntityDiesProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(GreyflashEntity.class)
public abstract class GreyflashEntityMixin implements IEntityNovel {
    @Override
    public void hyperdaimc$novelRemove(Entity.RemovalReason reason) {
        GreyflashEntity self = (GreyflashEntity) (Object) this;
        self.setRemoved(reason);
        GreyflashEntityDiesProcedure.execute(self.level(), self.getX(), self.getY(), self.getZ());
    }
}
