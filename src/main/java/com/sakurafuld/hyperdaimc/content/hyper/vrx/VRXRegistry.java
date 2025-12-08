package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

@Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VRXRegistry {
    private static final List<Consumer<VRXRegistration>> REGISTRANTS = Lists.newArrayList();
    private static final Map<String, VRXType> REGISTRY = new Object2ObjectOpenHashMap<>();
    private static final List<VRXType.Converter> CONVERTERS = Lists.newArrayList();
    private static final List<VRXType.Caster> CASTERS = Lists.newArrayList();
    private static final List<VRXType.Collector> COLLECTORS = Lists.newArrayList();
    private static final TagKey<Item> TAG_VRX_BANNED = ItemTags.create(identifier("vrx_banned"));

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void startup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            VRXRegistration registration = new VRXRegistration(VRXRegistry::registerType, VRXRegistry::registerCollector);
            registration.registerConversionCastingFilter(stack ->
                    !(HyperCommonConfig.VRX_SEAL_HYPERLINK.get() && ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().equals(HYPERDAIMC)));
            registration.registerConversionCastingFilter(stack -> !stack.is(TAG_VRX_BANNED));

            for (Consumer<VRXRegistration> registrant : REGISTRANTS)
                registrant.accept(registration);

            CONVERTERS.addAll(registration.converters);
            CASTERS.addAll(registration.casters);
        });
    }

    public static void registerRegistrant(Consumer<VRXRegistration> registrant) {
        REGISTRANTS.add(registrant);
    }

    private static VRXType registerType(String name, Supplier<VRXOne> creator, VRXType.Checker checker, boolean client, int priority) {
        VRXType type = new VRXType(name, creator, checker, client, priority);
        if (REGISTRY.put(name, type) != null)
            throw new IllegalStateException("VRXType '%s' already exists".formatted(name));
        return type;
    }

    private static void registerCollector(VRXType.Collector collector) {
        COLLECTORS.add(collector);
    }

    public static VRXType get(String name) {
        return REGISTRY.getOrDefault(name, VRXType.empty());
    }

    public static String[] allNames() {
        return REGISTRY.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .sorted(Comparator.comparingInt(entry -> entry.getValue().priority()))
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<VRXType> check(CapabilityProvider<?> provider, Direction face) {
        List<VRXType> list = Lists.newArrayList();
        for (VRXType type : REGISTRY.values()) {
            if (type.check(provider, face))
                list.add(type);
        }

        return list;
    }

    public static VRXOne convert(ItemStack stack, List<VRXType> available) {
        if (!stack.isEmpty()) {
            for (VRXType.Converter converter : CONVERTERS) {
                @Nullable
                VRXOne converted = converter.convert(stack);
                if (converted == null)
                    return VRXOne.EMPTY;
                if (!converted.isEmpty() && available.contains(converted.type))
                    return converted;
            }
            if (!available.contains(VRXType.item()))
                return VRXOne.EMPTY;

            return new VRXOneItem(stack);
        }

        return VRXOne.EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <I> VRXJeiWrapper<I> cast(I ingredient, List<VRXType> available) {
        for (VRXType.Caster caster : CASTERS) {
            @Nullable
            VRXJeiWrapper<?> wrapper = caster.cast(ingredient);
            if (wrapper == null)
                return VRXJeiWrapper.empty();
            if (!wrapper.isEmpty() && available.contains(wrapper.type()))
                return (VRXJeiWrapper<I>) wrapper;
        }

        return VRXJeiWrapper.empty();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        List<VRXOne> list = Lists.newArrayList();
        for (VRXType.Collector collector : COLLECTORS)
            list.addAll(collector.collect(provider, face));

        return list;
    }
}
