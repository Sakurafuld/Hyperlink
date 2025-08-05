package com.sakurafuld.hyperdaimc.content.crafting.material;

import com.sakurafuld.hyperdaimc.api.content.GashatItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class MaterialItem extends Item {
    private final ResourceLocation model;
    private final boolean scaling;
    private final boolean coloring;
    private final boolean rotation;
    private final boolean particle;
    public final int[] tint;

    public MaterialItem(String name, Properties pProperties, boolean scaling, boolean coloring, boolean rotation, boolean particle, int... tint) {
        super(pProperties);
        this.model = identifier("special/" + name);
        this.scaling = scaling;
        this.coloring = coloring;
        this.rotation = rotation;
        this.particle = particle;
        this.tint = tint;
    }

    public int getTint(int index) {
        return this.tint[Math.min(this.tint.length - 1, index)];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GashatItemRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer == null ? this.renderer = new GashatItemRenderer(MaterialItem.this.model, MaterialItem.this.scaling, MaterialItem.this.coloring, MaterialItem.this.rotation, MaterialItem.this.particle) : this.renderer;

            }
        });
    }
//
//    @Override
//    public int getUseDuration(ItemStack pStack) {
//        return 32;
//    }
//
//    @Override
//    public UseAnim getUseAnimation(ItemStack pStack) {
//        return UseAnim.BOW;
//    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
//        ItemStack material = pPlayer.getItemInHand(pUsedHand);
//        boolean hasRecipe = pLevel.getRecipeManager().getAllRecipesFor(HyperRecipes.DESK.get()).stream()
//                .filter(recipe -> recipe.getResultItem(pLevel.registryAccess()).is(material.getItem()))
//                .anyMatch(recipe -> !recipe.getIngredients().isEmpty());
//
//        if (hasRecipe) {
//            pPlayer.startUsingItem(pUsedHand);
//            return InteractionResultHolder.consume(material);
//        } else {
//            return InteractionResultHolder.pass(material);
//        }
//    }
//
//    @Override
//    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
//        List<ItemStack> ingredients = pLevel.getRecipeManager().getAllRecipesFor(HyperRecipes.DESK.get()).stream()
//                .filter(recipe -> recipe.getResultItem(pLevel.registryAccess()).is(pStack.getItem()))
//                .flatMap(recipe -> recipe.getIngredients().stream())
//                .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
//                .toList();
//
//        ItemStack stack = ItemHandlerHelper.copyStackWithSize(ingredients.get(pLevel.getRandom().nextInt(ingredients.size())), 1);
//        Vec3 view = pLivingEntity.getViewVector(1);
//        Vec3 position = pLivingEntity.getEyePosition().add(view.scale(0.5));
//
//        for (int count = 0; count < ingredients.size(); count++) {
//            release(pLevel, stack, position, view);
//        }
//
//        pLevel.playSound(null, pLivingEntity.blockPosition(), SoundEvents.ITEM_BREAK, pLivingEntity.getSoundSource(), 1, 0.4f + pLevel.getRandom().nextFloat() * 0.4f);
//        if (!(pLivingEntity instanceof Player player && player.getAbilities().instabuild)) {
//            pStack.shrink(1);
//        }
//
//        return pStack;
//    }
//
//    private static void release(Level level, ItemStack stack, Vec3 position, Vec3 vec) {
//        ItemEntity entity = new ItemEntity(level, position.x(), position.y(), position.z(), stack.copy());
//        double multiplier = level.getRandom().nextDouble() * 0.1 + 0.2;
//        entity.setDeltaMovement(level.getRandom().nextGaussian() * 0.004 * 24 + vec.x() * multiplier, level.getRandom().nextGaussian() * 0.004 * 24 + vec.y() * multiplier, level.getRandom().nextGaussian() * 0.004 * 24 + vec.z() * multiplier);
//        entity.setPickUpDelay(10);
//        level.addFreshEntity(entity);
//    }
}
