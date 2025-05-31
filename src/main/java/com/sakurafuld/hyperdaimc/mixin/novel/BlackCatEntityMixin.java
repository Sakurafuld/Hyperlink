package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import net.mcreator.dragionnsstuff.entity.BlackCatEntity;
import net.mcreator.dragionnsstuff.network.DragionnsStuffModVariables;
import net.mcreator.dragionnsstuff.procedures.BlackCatEntityDiesProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(BlackCatEntity.class)
public abstract class BlackCatEntityMixin implements IEntityNovel {
    @Override
    public void novelRemove(Entity.RemovalReason reason) {
        BlackCatEntity self = (BlackCatEntity) (Object) this;
        self.setRemoved(reason);
        BlackCatEntityDiesProcedure.execute(self.level(), self.getX(), self.getY(), self.getZ());
        DragionnsStuffModVariables.WorldVariables.get(self.level()).BlackCatHealth = 0;
        DragionnsStuffModVariables.WorldVariables.get(self.level()).syncData(self.level());
    }
}
