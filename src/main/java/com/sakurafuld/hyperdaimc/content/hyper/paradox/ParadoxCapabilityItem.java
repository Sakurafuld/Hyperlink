package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@AutoRegisterCapability
@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ParadoxCapabilityItem implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<ParadoxCapabilityItem> TOKEN = CapabilityManager.get(new CapabilityToken<>() {
    });

    private final LazyOptional<ParadoxCapabilityItem> capability = LazyOptional.of(() -> this);

    @Nullable
    private ParadoxChainCluster cluster = null;

    @Nullable
    public ParadoxChainCluster getCluster() {
        return this.cluster;
    }

    public void setCluster(@Nullable ParadoxChainCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == TOKEN && HyperCommonConfig.ENABLE_PARADOX.get() ? this.capability.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.cluster == null ? new CompoundTag() : this.cluster.serialize();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.cluster = nbt.isEmpty() ? null : new ParadoxChainCluster(nbt);
    }
}
