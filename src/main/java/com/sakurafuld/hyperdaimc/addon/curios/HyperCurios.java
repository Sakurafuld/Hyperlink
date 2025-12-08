package com.sakurafuld.hyperdaimc.addon.curios;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.CURIOS;

@AddonMod(CURIOS)
public class HyperCurios {
    private final ICurioItem curio = new ICurioItem() {
        @Override
        public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
            return HyperCommonConfig.ENABLE_MUTEKI.get();
        }

        @Override
        public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
            if (!HyperCommonConfig.ENABLE_MUTEKI.get())
                return ICurioItem.super.getEquipSound(slotContext, stack);
            return new ICurio.SoundInfo(HyperSounds.MUTEKI.get(), 1, 1);
        }
    };

    public HyperCurios() {
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, this::attachCapability);
    }

    private void attachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.is(HyperItems.MUTEKI.get()))
            event.addCapability(CuriosCapability.ID_ITEM, CurioItemCapability.createProvider(new ItemizedCurioCapability(this.curio, stack)));
    }
}
