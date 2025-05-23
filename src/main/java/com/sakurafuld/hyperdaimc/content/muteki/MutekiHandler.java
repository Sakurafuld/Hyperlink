package com.sakurafuld.hyperdaimc.content.muteki;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
        try {
            return HyperServerConfig.ENABLE_MUTEKI.get() && Check.INSTANCE.isMuteki(entity);
        } catch (Throwable ignore) {
            LOG.debug("MutekiError!!");
        }
        LOG.debug("MutekiError!!");
        return false;
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

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void postmortal(LivingDeathEvent event) {

        LivingEntity entity = event.getEntityLiving();
        if ((!Float.isFinite(entity.getHealth()) || HyperServerConfig.MUTEKI_NOVEL.get() || !NovelHandler.novelized(entity)) && muteki(entity)) {

            event.setCanceled(true);
        } else if (NovelHandler.novelized(entity)) {

            ((IEntityNovel) entity).killsOver();
            event.setCanceled(false);
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
                if (!HyperServerConfig.ENABLE_MUTEKI.get()) {
                    return ICurioItem.super.getEquipSound(slotContext, stack);
                }
                return new ICurio.SoundInfo(HyperSounds.MUTEKI.get(), 1f, 1f);
            }
        };

        public ICapabilityProvider capability(ItemStack stack) {
            return CurioItemCapability.createProvider(new ItemizedCurioCapability(this.CURIO, stack));
        }
    }

    private enum Check {
        INSTANCE;

        public boolean isMuteki(Entity entity) {
            if (entity instanceof LivingEntity living) {
                if (require(CURIOS).ready() && !CuriosApi.getCuriosHelper().findCurios(living, HyperItems.MUTEKI.get()).isEmpty()) {
                    return true;
                }
                if (living instanceof Player player) {
                    for (int index = 0; index < Inventory.getSelectionSize(); index++) {
                        if (player.getInventory().getItem(index).is(HyperItems.MUTEKI.get())) {
                            return true;
                        }
                    }
                }
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (living.getItemBySlot(slot).is(HyperItems.MUTEKI.get())) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
