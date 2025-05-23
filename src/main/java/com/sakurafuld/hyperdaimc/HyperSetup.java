package com.sakurafuld.hyperdaimc;

import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.compat.VRXOneGas;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperMenus;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntityRenderer;
import com.sakurafuld.hyperdaimc.content.fumetsu.squall.FumetsuSquallRenderer;
import com.sakurafuld.hyperdaimc.content.fumetsu.storm.FumetsuStormRenderer;
import com.sakurafuld.hyperdaimc.content.fumetsu.skull.FumetsuSkullRenderer;
import com.sakurafuld.hyperdaimc.content.vrx.VRXOne;
import com.sakurafuld.hyperdaimc.content.vrx.VRXOverlay;
import com.sakurafuld.hyperdaimc.content.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.vrx.VRXTooltip;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;

import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

public class HyperSetup {
    public static final Set<ResourceLocation> specialModels = Sets.newHashSet();

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.initialize();
            require(MEKANISM).run(VRXOneGas::initialize);
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


            MenuScreens.register(HyperMenus.VRX.get(), VRXScreen::new);

            OverlayRegistry.registerOverlayTop("vrx", new VRXOverlay());

            MinecraftForgeClient.registerTooltipComponentFactory(VRXTooltip.class, VRXTooltip.Client::new);
        });
    }

    @SubscribeEvent
    public void attributeCreation(EntityAttributeCreationEvent event) {
        event.put(HyperEntities.FUMETSU.get(), FumetsuEntity.createAttributes());
    }

    @SubscribeEvent
    public void registerSlot(InterModEnqueueEvent event) {
        event.enqueueWork(() -> require(CURIOS).run(() ->
                InterModComms.sendTo(CURIOS, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("maximum")
                        .icon(identifier(HYPERDAIMC, "item/empty_maximum_slot"))
                        .priority(Integer.MIN_VALUE)
                        .build())));


    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerTexture(TextureStitchEvent.Pre event) {
        require(CURIOS).run(() ->
                event.addSprite(identifier(HYPERDAIMC, "item/empty_maximum_slot")));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerModel(ModelRegistryEvent event) {
        for (ResourceLocation identifier : specialModels) {
            ForgeModelBakery.addSpecialModel(identifier);
        }
    }
}
