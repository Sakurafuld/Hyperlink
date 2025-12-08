package com.sakurafuld.hyperdaimc;

import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.content.*;
import com.sakurafuld.hyperdaimc.content.crafting.chemical.ChemicalEntity;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskScreen;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlockEntityRenderer;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntityRenderer;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkullRenderer;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.squall.FumetsuSquallRenderer;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.storm.FumetsuStormRenderer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxTooltip;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOverlay;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXTooltip;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerScreen;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Set;

public class HyperSetup {
    public static final Set<ResourceLocation> specialModels = Sets.newHashSet();

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            HyperConnection.initialize();

            BrewingRecipeRegistry.addRecipe(new IBrewingRecipe() {
                @Override
                public boolean isInput(ItemStack input) {
                    if (!HyperCommonConfig.FUMETSU_RECIPE.get())
                        return false;

                    return input.is(Items.POTION) || input.is(Items.SPLASH_POTION) || input.is(Items.LINGERING_POTION);
                }

                @Override
                public boolean isIngredient(ItemStack ingredient) {
                    if (!HyperCommonConfig.FUMETSU_RECIPE.get())
                        return false;

                    return ingredient.is(HyperItems.GOD_SIGIL.get());
                }

                @Override
                public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                    if (!HyperCommonConfig.FUMETSU_RECIPE.get())
                        return ItemStack.EMPTY;

                    return this.isInput(input) && this.isIngredient(ingredient) ? HyperItems.CHEMICAL_MAX.get().getDefaultInstance() : ItemStack.EMPTY;
                }
            });

            BrewingRecipeRegistry.addRecipe(new IBrewingRecipe() {
                @Override
                public boolean isInput(ItemStack input) {
                    if (!HyperCommonConfig.FUMETSU_RECIPE.get())
                        return false;

                    return input.is(HyperItems.CHEMICAL_MAX.get());
                }

                @Override
                public boolean isIngredient(ItemStack ingredient) {
                    if (!HyperCommonConfig.FUMETSU_RECIPE.get())
                        return false;

                    return ingredient.is(Items.NETHER_STAR);
                }

                @Override
                public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                    if (!HyperCommonConfig.FUMETSU_RECIPE.get())
                        return ItemStack.EMPTY;

                    return this.isInput(input) && this.isIngredient(ingredient) ? HyperBlocks.FUMETSU_SKULL.get().asItem().getDefaultInstance() : ItemStack.EMPTY;
                }
            });

            DispenserBlock.registerBehavior(HyperItems.CHEMICAL_MAX.get(), new DispenseItemBehavior() {
                @Override
                public ItemStack dispense(BlockSource pSource, ItemStack pStack) {
                    return (new AbstractProjectileDispenseBehavior() {
                        @Override
                        protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack) {
                            return Util.make(new ChemicalEntity(HyperEntities.CHEMICAL_MAX.get(), pLevel), chemical -> {
                                chemical.setPos(pPosition.x(), pPosition.y(), pPosition.z());
                                chemical.setItem(pStack);
                            });
                        }

                        @Override
                        protected float getPower() {
                            return super.getPower() * 1.25f;
                        }

                        @Override
                        protected float getUncertainty() {
                            return super.getUncertainty() * 0.5f;
                        }
                    }).dispense(pSource, pStack);
                }
            });
        });
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            EntityRenderers.register(HyperEntities.FUMETSU.get(), FumetsuEntityRenderer::new);
            EntityRenderers.register(HyperEntities.FUMETSU_SKULL.get(), FumetsuSkullRenderer::new);
            EntityRenderers.register(HyperEntities.FUMETSU_STORM.get(), FumetsuStormRenderer::new);
            EntityRenderers.register(HyperEntities.FUMETSU_STORM_SKULL.get(), FumetsuSkullRenderer::new);
            EntityRenderers.register(HyperEntities.FUMETSU_SQUALL.get(), FumetsuSquallRenderer::new);
            EntityRenderers.register(HyperEntities.CHEMICAL_MAX.get(), ThrownItemRenderer::new);

            MenuScreens.register(HyperMenus.VRX.get(), VRXScreen::new);
            MenuScreens.register(HyperMenus.DESK.get(), DeskScreen::new);
            MenuScreens.register(HyperMenus.MATERIALIZER.get(), MaterializerScreen::new);
        });
    }

    @SubscribeEvent
    public void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("vrx", new VRXOverlay());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(VRXTooltip.class, VRXTooltip.Client::new);
        event.register(ParadoxTooltip.class, ParadoxTooltip.Client::new);
    }

    @SubscribeEvent
    public void attributeCreation(EntityAttributeCreationEvent event) {
        event.put(HyperEntities.FUMETSU.get(), FumetsuEntity.createAttributes());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerModel(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation identifier : specialModels) {
            event.register(identifier);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(HyperBlockEntities.FUMETSU_SKULL.get(), FumetsuSkullBlockEntityRenderer::new);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerSkull(EntityRenderersEvent.CreateSkullModels event) {
        SkullBlockRenderer.SKIN_BY_TYPE.put(FumetsuSkullBlock.CENTER, FumetsuSkullBlockEntityRenderer.TEXTURE);
        SkullBlockRenderer.SKIN_BY_TYPE.put(FumetsuSkullBlock.RIGHT, FumetsuSkullBlockEntityRenderer.TEXTURE);
        SkullBlockRenderer.SKIN_BY_TYPE.put(FumetsuSkullBlock.LEFT, FumetsuSkullBlockEntityRenderer.TEXTURE);
        event.registerSkullModel(FumetsuSkullBlock.CENTER, new SkullModel(FumetsuSkullBlockEntityRenderer.create(0, 0).bakeRoot()));
        event.registerSkullModel(FumetsuSkullBlock.RIGHT, new SkullModel(FumetsuSkullBlockEntityRenderer.create(0, 16).bakeRoot()));
        event.registerSkullModel(FumetsuSkullBlock.LEFT, new SkullModel(FumetsuSkullBlockEntityRenderer.create(32, 0).bakeRoot()));
    }
}
