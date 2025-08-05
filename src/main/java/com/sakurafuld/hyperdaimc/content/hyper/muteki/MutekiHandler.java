package com.sakurafuld.hyperdaimc.content.hyper.muteki;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class MutekiHandler {
    public static final SimpleCommandExceptionType ERROR_NOT_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("argument.muteki.notfound"));
    public static final SimpleCommandExceptionType ERROR_REQUIRE = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.muteki"));
    public static boolean specialGameModeSwitch = false;

    public static boolean muteki(LivingEntity entity) {
        return ((ILivingEntityMuteki) entity).muteki();
    }

    public static boolean checkMuteki(LivingEntity entity) {
        ((ILivingEntityMuteki) entity).force(true);
        boolean muteki = HyperCommonConfig.ENABLE_MUTEKI.get() && Check.INSTANCE.isMuteki(entity);
        ((ILivingEntityMuteki) entity).force(false);
        return muteki;
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        require(CURIOS).run(() -> {
            ItemStack stack = event.getObject();
            if (stack.is(HyperItems.MUTEKI.get())) {
                event.addCapability(identifier(CURIOS, "item"), Attach.INSTANCE.capability(stack));
            }
        });
    }

    private enum Check {
        INSTANCE;

        public boolean isMuteki(LivingEntity entity) {
            if (require(CURIOS).ready() && !CuriosApi.getCuriosHelper().findCurios(entity, HyperItems.MUTEKI.get()).isEmpty()) {
                return true;
            }
            if (entity instanceof Player player) {
                for (int index = 0; index < Inventory.getSelectionSize(); index++) {
                    if (player.getInventory().getItem(index).is(HyperItems.MUTEKI.get())) {
                        return true;
                    }
                }
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (entity.getItemBySlot(slot).is(HyperItems.MUTEKI.get())) {
                    return true;
                }
            }

            return false;
        }
    }

    private enum Attach {
        INSTANCE;

        private final ICurioItem CURIO = new ICurioItem() {
            @Override
            public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                return true;
            }

            @Override
            public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
                if (!HyperCommonConfig.ENABLE_MUTEKI.get()) {
                    return ICurioItem.super.getEquipSound(slotContext, stack);
                }
                return new ICurio.SoundInfo(HyperSounds.MUTEKI.get(), 1f, 1f);
            }
        };

        public ICapabilityProvider capability(ItemStack stack) {
            return CurioItemCapability.createProvider(new ItemizedCurioCapability(this.CURIO, stack));
        }
    }
}
