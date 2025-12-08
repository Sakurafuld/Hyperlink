package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXJeiWrapper;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSlot;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VRXGhostIngredientHandler implements IGhostIngredientHandler<VRXScreen> {
    @Override
    @SuppressWarnings("unchecked")
    public <I> @NotNull List<Target<I>> getTargetsTyped(@NotNull VRXScreen screen, @NotNull ITypedIngredient<I> ingredient, boolean doStart) {
        if (HyperCommonConfig.VRX_JEI.get()) {
            VRXJeiWrapper<I> wrapper = VRXRegistry.cast(ingredient.getIngredient(), screen.getMenu().getAvailableTypes());
            if (!wrapper.isEmpty()) {
                return screen.getMenu().slots.stream()
                        .filter(slot -> ingredient.getIngredient() instanceof ItemStack || slot instanceof VRXSlot)
                        .map(slot -> {
                            if (slot instanceof VRXSlot vrxSlot)
                                return new WrappedGhostTarget<>(screen, vrxSlot, wrapper);
                            else
                                return (Target<I>) new SimpleGhostTarget(screen, slot);
                        })
                        .toList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }
}
