package com.sakurafuld.hyperdaimc.content;

import com.mojang.serialization.Codec;
import com.sakurafuld.hyperdaimc.api.content.GashatParticle;
import com.sakurafuld.hyperdaimc.api.content.GashatParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HyperParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, HYPERDAIMC);

    public static final RegistryObject<ParticleType<GashatParticleOptions>> GASHAT;

    static {
        GASHAT = register("gashat", false, GashatParticleOptions.DESERIALIZER, GashatParticleOptions.CODEC);
    }

    public static <T extends ParticleOptions> RegistryObject<ParticleType<T>> register(String name, boolean alwaysRender, ParticleOptions.Deserializer<T> deserializer, Codec<T> codec) {
        return REGISTRY.register(name, () -> new ParticleType<>(alwaysRender, deserializer) {
            @Override
            public Codec<T> codec() {
                return codec;
            }
        });
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpecial(GASHAT.get(), new GashatParticle.Provider());
    }
}
