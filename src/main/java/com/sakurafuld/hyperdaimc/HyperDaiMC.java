package com.sakurafuld.hyperdaimc;

import com.sakurafuld.hyperdaimc.compat.mekanism.HyperMekanism;
import com.sakurafuld.hyperdaimc.compat.tconstruct.HyperTConstruct;
import com.sakurafuld.hyperdaimc.content.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mod(HYPERDAIMC)
public class HyperDaiMC {
    @SuppressWarnings("removal")
    public HyperDaiMC() {
        this(FMLJavaModLoadingContext.get());
    }

    public HyperDaiMC(FMLJavaModLoadingContext context) {
        LOG.debug("hyperdaimc Wakeup");
        IEventBus bus = context.getModEventBus();

        bus.register(new HyperSetup());
        MinecraftForge.EVENT_BUS.addListener(this::loggedIn);

        HyperBlockEntities.REGISTRY.register(bus);
        HyperBlocks.REGISTRY.register(bus);
        HyperEntities.REGISTRY.register(bus);
        HyperItems.REGISTRY.register(bus);
        HyperMenus.REGISTRY.register(bus);
        HyperParticles.REGISTRY.register(bus);
        HyperRecipes.TYPE_REGISTRY.register(bus);
        HyperRecipes.SERIALIZER_REGISTRY.register(bus);
        HyperSounds.REGISTRY.register(bus);
        HyperTabs.REGISTRY.register(bus);

        context.registerConfig(ModConfig.Type.COMMON, HyperCommonConfig.SPEC, HYPERDAIMC + "-common.toml");

        new HyperMekanism(context);
        new HyperTConstruct(context);
    }

    private void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (HyperCommonConfig.CONFIG_WARNING.get()) {
            event.getEntity().displayClientMessage(Component.literal("[Hyperlink]").withStyle(ChatFormatting.AQUA), false);
            event.getEntity().displayClientMessage(Component.translatable("chat.hyperdaimc.config_warning").withStyle(ChatFormatting.YELLOW), false);
        }
    }
}
