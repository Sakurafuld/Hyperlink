package com.sakurafuld.hyperdaimc;

import com.sakurafuld.hyperdaimc.content.*;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

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

        context.registerConfig(ModConfig.Type.COMMON, HyperCommonConfig.SPEC);

        this.addOn(context, context.getContainer().getModInfo().getOwningFile().getFile().getScanResult().getAnnotations());
    }

    @SuppressWarnings("unchecked")
    private void addOn(FMLJavaModLoadingContext context, Set<ModFileScanData.AnnotationData> annotations) {
        annotations.stream()
                .filter(annotation -> annotation.annotationType().equals(AddonMod.TYPE))
                .forEach(annotation -> {
                    try {
                        Class<?> clazz = Class.forName(annotation.clazz().getClassName(), false, this.getClass().getClassLoader());
                        List<String> dependencies = (List<String>) annotation.annotationData().get("value");
                        if (requireAll(dependencies.toArray(new String[0]))) {
                            LOG.info("Loaded @AddonMod {}", clazz.getSimpleName());
                            for (Constructor<?> constructor : clazz.getConstructors()) {
                                int parameter = constructor.getParameterCount();
                                if (parameter == 0) {
                                    LOG.info("Construct @AddonMod {} with no param", clazz.getSimpleName());
                                    constructor.newInstance();
                                    break;
                                } else if (parameter == 1 && constructor.getParameterTypes()[0].equals(FMLJavaModLoadingContext.class)) {
                                    LOG.info("Construct @AddonMod {} with context param", clazz.getSimpleName());
                                    constructor.newInstance(context);
                                    break;
                                } else
                                    throw new RuntimeException("No valid constructor found @AddonMod " + clazz.getSimpleName());
                            }
                        } else {
                            String s = String.join("|", dependencies);
                            LOG.info("Didn't load @AddonMod {}, because dependencies({}) is not loaded", clazz.getSimpleName(), s);
                        }
                    } catch (Throwable e) {
                        LOG.info("Failed to load @AddonMod! {}", e.toString());
                    }
                });
    }

    private void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (HyperCommonConfig.CONFIG_WARNING.get()) {
            event.getEntity().displayClientMessage(Component.literal("[Hyperlink]").withStyle(ChatFormatting.AQUA), false);
            event.getEntity().displayClientMessage(Component.translatable("chat.hyperdaimc.config_warning").withStyle(ChatFormatting.YELLOW), false);
        }
    }
}
