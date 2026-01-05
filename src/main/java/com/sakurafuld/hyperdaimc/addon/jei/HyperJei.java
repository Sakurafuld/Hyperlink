package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskMenu;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskScreen;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerHandler;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerMenu;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

@JeiPlugin
public class HyperJei implements IModPlugin {
    private static final ResourceLocation ID = identifier("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerRecipes(IRecipeRegistration registration) {
        this.addBrewing(registration);
        this.addInformation(registration);

        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        registration.addRecipes(DeskRecipeCategory.TYPE, level.getRecipeManager().getAllRecipesFor(HyperRecipes.DESK.get()));
        registration.addRecipes(MaterializerRecipeCategory.TYPE, MaterializerHandler.loadRecipe(level));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new DeskRecipeCategory(helper),
                new MaterializerRecipeCategory(helper),
                new HyperBrewingRecipeCategory(helper),
                new HyperInformationRecipeCategory());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(HyperBlocks.DESK.get(), DeskRecipeCategory.TYPE);
        registration.addRecipeCatalyst(HyperBlocks.MATERIALIZER.get(), MaterializerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(Blocks.BREWING_STAND, HyperBrewingRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(DeskMenu.class, null, DeskRecipeCategory.TYPE, 1, 81, 82, 36);
        registration.addRecipeTransferHandler(MaterializerMenu.class, null, MaterializerRecipeCategory.TYPE, 0, 1, 3, 36);
        registration.addRecipeTransferHandler(BrewingStandMenu.class, MenuType.BREWING_STAND, HyperBrewingRecipeCategory.TYPE, 0, 4, 5, 36);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(DeskScreen.class, 56, 186, 14, 15, DeskRecipeCategory.TYPE);
        registration.addRecipeClickArea(MaterializerScreen.class, 46, 56, 94, 9, MaterializerRecipeCategory.TYPE);

        registration.addGhostIngredientHandler(VRXScreen.class, new VRXGhostIngredientHandler());
    }

    private void addBrewing(IRecipeRegistration registration) {
        if (!HyperCommonConfig.FUMETSU_RECIPE.get())
            return;

        HyperBrewingRecipe chemicalMAX = HyperBrewingRecipe.bugStar(
                identifier("chemical_max"),
                List.of(HyperItems.CHEMICAL_MAX.get().getDefaultInstance()),
                Stream.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)
                        .flatMap(bottle -> StreamSupport.stream(ForgeRegistries.POTIONS.spliterator(), false)
                                .filter(potion -> potion != Potions.EMPTY)
                                .map(potion -> PotionUtils.setPotion(new ItemStack(bottle), potion)))
                        .toList(),
                HyperItems.GOD_SIGIL.get().getDefaultInstance());

        HyperBrewingRecipe fumetsuSkulls = HyperBrewingRecipe.skulls(
                identifier("fumetsu_skull"),
                Stream.of(HyperBlocks.FUMETSU_LEFT, HyperBlocks.FUMETSU_SKULL, HyperBlocks.FUMETSU_RIGHT)
                        .map(object -> object.get().asItem().getDefaultInstance())
                        .toList(),
                List.of(HyperItems.CHEMICAL_MAX.get().getDefaultInstance()),
                Items.NETHER_STAR.getDefaultInstance());
        registration.addRecipes(HyperBrewingRecipeCategory.TYPE, List.of(chemicalMAX, fumetsuSkulls));
    }

    private void addInformation(IRecipeRegistration registration) {
        registration.addRecipes(HyperInformationRecipeCategory.TYPE, List.of(new ChemicalInformation(registration.getJeiHelpers().getGuiHelper())));
        if (HyperCommonConfig.FUMETSU_SUMMON.get()) {
            registration.addRecipes(HyperInformationRecipeCategory.TYPE, List.of(new FumetsuInformation(registration.getJeiHelpers().getGuiHelper())));
//            List<ItemStack> fumetsuWither = Stream.of(HyperItems.GOD_SIGIL.get(), HyperBlocks.SOUL.get().asItem(), HyperBlocks.FUMETSU_LEFT.get().asItem(), HyperBlocks.FUMETSU_SKULL.get().asItem(), HyperBlocks.FUMETSU_RIGHT.get().asItem(), HyperItems.BUG_STARS.get(0).get())
//                    .map(Item::getDefaultInstance)
//                    .toList();
//            List<String> fumetsuWitherParameter = fumetsuWither.stream()
//                    .map(stack -> I18n.get(stack.getDescriptionId()))
//                    .collect(Collectors.toList());
//            fumetsuWitherParameter.add(HyperEntities.FUMETSU.get().getDescription().getString());
//
//            registration.addIngredientInfo(fumetsuWither, VanillaTypes.ITEM_STACK, Component.translatable("information.hyperdaimc.fumetsu_wither.0", fumetsuWitherParameter.toArray(Object[]::new)));
        }
    }
}
