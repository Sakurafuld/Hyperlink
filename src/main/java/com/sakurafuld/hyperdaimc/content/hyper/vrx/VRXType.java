package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@SuppressWarnings({"ClassCanBeRecord", "UnstableApiUsage"})
@Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VRXType implements Comparable<VRXType> {
    static VRXType empty;
    static VRXType item;
    static VRXType fluid;
    static VRXType energy;

    public static VRXType empty() {
        assert empty != null;
        return empty;
    }

    public static VRXType item() {
        assert item != null;
        return item;
    }

    public static VRXType fluid() {
        assert fluid != null;
        return fluid;
    }

    public static VRXType energy() {
        assert energy != null;
        return energy;
    }

    public final String name;
    private final Supplier<VRXOne> creator;
    private final Checker checker;
    private final boolean client;
    private final int priority;

    VRXType(String name, Supplier<VRXOne> creator, Checker checker, boolean client, int priority) {
        this.name = name;
        this.creator = creator;
        this.checker = checker;
        this.client = client;
        this.priority = priority;
    }

    public boolean isEmpty() {
        return this == empty;
    }

    public boolean check(CapabilityProvider<?> provider, Direction face) {
        return this.checker.check(provider, face);
    }

    public boolean workOnClient() {
        return this.client;
    }

    public int priority() {
        return this.priority;
    }

    public CompoundTag serialize(VRXOne self) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.name);
        tag.put("Data", self.save());
        return tag;
    }

    public static VRXOne deserializeStatic(CompoundTag tag) {
        VRXType type = VRXRegistry.get(tag.getString("Type"));
        if (type.isEmpty())
            return VRXOne.EMPTY;

        VRXOne one = type.creator.get();
        one.load(tag.getCompound("Data"));
        return one;
    }

    @Override
    public int compareTo(@NotNull VRXType o) {
        return Integer.compare(this.priority(), o.priority());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXType type = (VRXType) object;
        return Objects.equals(name, type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @FunctionalInterface
    public interface Checker {
        boolean check(CapabilityProvider<?> provider, Direction face);
    }

    @FunctionalInterface
    public interface Converter {
        @Nullable VRXOne convert(ItemStack stack);
    }

    @FunctionalInterface
    public interface Collector {
        List<VRXOne> collect(CapabilityProvider<?> provider, Direction face);
    }

    @FunctionalInterface
    public interface Caster {
        <I> @Nullable VRXJeiWrapper<?> cast(I ingredient);
    }

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> VRXRegistry.registerRegistrant(registration -> {
            empty = registration.registerType("empty", () -> VRXOne.EMPTY, (provider, face) -> false, false, Integer.MAX_VALUE);

            item = registration.registerType("item", VRXOneItem::new, VRXOneItem::check, false, 0);
//            ↓VRXRegistry.convert()でのフォールバック挙動なのでここでは登録しないよ.
//            registration.registerConverter(VRXOneItem::new);
            registration.registerCaster(VRXOneItem::cast);
            registration.registerCollector(VRXOneItem::collect);

            fluid = registration.registerType("fluid", VRXOneFluid::new, VRXOneFluid::check, false, 100);
            registration.registerConverter(VRXOneFluid::convert);
            registration.registerCaster(VRXOneFluid::cast);
            registration.registerCollector(VRXOneFluid::collect);

            energy = registration.registerType("forge:energy", VRXOneEnergy::new, VRXOneEnergy::check, false, 250);
            registration.registerConverter(VRXOneEnergy::convert);
            registration.registerCollector(VRXOneEnergy::collect);
        }));
    }
}
