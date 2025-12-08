package com.sakurafuld.hyperdaimc.content.hyper.muteki;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILivingEntityMuteki;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class MutekiHandler {
    public static final SimpleCommandExceptionType ERROR_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.muteki.notfound"));
    public static final SimpleCommandExceptionType ERROR_REQUIRE = new SimpleCommandExceptionType(Component.translatable("permissions.requires.muteki"));
    public static boolean specialGameModeSwitch = false;

    public static boolean muteki(LivingEntity entity) {
        return HyperCommonConfig.ENABLE_MUTEKI.get() && ((ILivingEntityMuteki) entity).hyperdaimc$muteki();
    }

    public static boolean checkMuteki(LivingEntity entity) {
        if (!HyperCommonConfig.ENABLE_MUTEKI.get()) return false;
        ((ILivingEntityMuteki) entity).hyperdaimc$mutekiForce(true);
        boolean muteki = Check.INSTANCE.isMuteki(entity);
        ((ILivingEntityMuteki) entity).hyperdaimc$mutekiForce(false);
        return muteki;
    }

    private enum Check {
        INSTANCE;

        private final EquipmentSlot[] equipments = EquipmentSlot.values();

        public boolean isMuteki(LivingEntity entity) {
            if (entity instanceof Player player)
                for (int index = 0; index < Inventory.getSelectionSize(); index++)
                    if (player.getInventory().getItem(index).is(HyperItems.MUTEKI.get()))
                        return true;

            for (EquipmentSlot slot : this.equipments)
                if (entity.getItemBySlot(slot).is(HyperItems.MUTEKI.get()))
                    return true;
            //noinspection RedundantIfStatement
            if (require(CURIOS) && CuriosApi.getCuriosInventory(entity).filter(handler -> !handler.findCurios(HyperItems.MUTEKI.get()).isEmpty()).isPresent())
                return true;

            return false;
        }
    }
}
