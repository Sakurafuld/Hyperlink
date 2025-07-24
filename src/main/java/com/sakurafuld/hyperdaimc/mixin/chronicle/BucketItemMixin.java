package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item {
    @Shadow
    @Final
    private Fluid content;

    public BucketItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @ModifyVariable(method = "use", at = @At("STORE"), ordinal = 1)
    private ItemStack use(ItemStack value, Level pLevel, Player pPlayer) {
        BlockHitResult hit = getPlayerPOVHitResult(pLevel, pPlayer, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (ChronicleHandler.isPaused(pLevel, hit.getBlockPos(), null)) {
            return ItemStack.EMPTY;
        } else {
            return value;
        }
    }
}
