package com.sakurafuld;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class BM115 extends Item {
    private ResourceKey<Level> dimension = null;
    private BlockPos pos = null;

    public BM115(Properties pProperties) {
        super(pProperties);
    }

    public void set(ResourceKey<Level> dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void clear() {
        this.dimension = null;
        this.pos = null;
    }

//    @Override
//    public InteractionResult useOn(UseOnContext context) {
//        if (context.isSecondaryUseActive()) {
//            context.getItemInHand().getCapability(BM115TestItemBombCapability.TOKEN).ifPresent(capability -> {
//                capability.set(context.getLevel().dimension(), context.getClickedPos());
//            });
//            return InteractionResult.SUCCESS;
//        }
//
//        return InteractionResult.PASS;
//    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
//        ItemStack stack = player.getItemInHand(hand);
//        if (!player.isShiftKeyDown()) {
//            stack.getCapability(BM115TestItemBombCapability.TOKEN).ifPresent(capability -> {
//                if (capability.getDimension() != null && capability.getPos() != null && level.dimension().equals(capability.getDimension())) {
//                    Vec3 center = Vec3.atCenterOf(capability.getPos());
//                    level.explode(player, center.x(), center.y(), center.z(), 2, Level.ExplosionInteraction.BLOCK);
//                }
//            });
//
//            return InteractionResultHolder.success(stack);
//        }
//
//        return InteractionResultHolder.pass(stack);
//    }

    @Override
    public boolean isComplex() {
        return super.isComplex();
    }
}
