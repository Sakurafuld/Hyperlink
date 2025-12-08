package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class VRXRegistration {
    private final TypeRegistrar registrar;
    public final List<VRXType.Converter> converters = Lists.newArrayList();
    public final List<VRXType.Caster> casters = Lists.newArrayList();
    private final Consumer<VRXType.Collector> collector;

    public VRXRegistration(TypeRegistrar registrar, Consumer<VRXType.Collector> collector) {
        this.registrar = registrar;
        this.collector = collector;
    }

    public VRXType registerType(String name, Supplier<VRXOne> creator, VRXType.Checker checker, boolean client, int priority) {
        return this.registrar.register(name, creator, checker, client, priority);
    }

    public void registerConverter(VRXType.Converter converter) {
        this.converters.add(converter);
    }

    public void registerConversionFilter(Predicate<ItemStack> filter) {
        this.converters.add(0, stack -> filter.test(stack) ? VRXOne.EMPTY : null);
    }

    public void registerCaster(VRXType.Caster caster) {
        this.casters.add(caster);
    }

    public void registerCastingFilter(Predicate<Object> filter) {
        this.casters.add(0, new VRXType.Caster() {
            @Override
            public @Nullable <I> VRXJeiWrapper<?> cast(I ingredient) {
                return filter.test(ingredient) ? VRXJeiWrapper.empty() : null;
            }
        });
    }

    public void registerConversionCastingFilter(Predicate<ItemStack> filter) {
        this.registerConversionFilter(filter);
        this.registerCastingFilter(ingredient -> !(ingredient instanceof ItemStack stack) || filter.test(stack));
    }

    public void registerCollector(VRXType.Collector collector) {
        this.collector.accept(collector);
    }

    @FunctionalInterface
    public interface TypeRegistrar {
        VRXType register(String name, Supplier<VRXOne> creator, VRXType.Checker checker, boolean client, int priority);
    }
}
