package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import net.mcreator.dragionnsstuff.entity.GreylightEntity;
import net.mcreator.dragionnsstuff.procedures.CatEntityDiesProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(GreylightEntity.class)
public abstract class GreylightEntityMixin implements IEntityNovel {
    @Override
    public void novelRemove(Entity.RemovalReason reason) {
        GreylightEntity self = (GreylightEntity) (Object) this;
        self.setRemoved(reason);
        CatEntityDiesProcedure.execute(self.level(), self.getX(), self.getY(), self.getZ());
    }
}
