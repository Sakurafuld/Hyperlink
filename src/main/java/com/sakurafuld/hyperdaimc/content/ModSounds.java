package com.sakurafuld.hyperdaimc.content;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.Deets.*;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY
            = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HYPERDAIMC);

    public static final RegistryObject<SoundEvent> MUTEKI;

    static {

        MUTEKI = REGISTRY.register("muteki_equip", ()-> new SoundEvent(identifier(HYPERDAIMC, "muteki_equip")));

    }
}
