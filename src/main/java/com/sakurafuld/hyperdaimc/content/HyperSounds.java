package com.sakurafuld.hyperdaimc.content;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class HyperSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY
            = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HYPERDAIMC);

    public static final RegistryObject<SoundEvent> MUTEKI;
    public static final RegistryObject<SoundEvent> NOVEL;
    public static final RegistryObject<SoundEvent> CHRONICLE_SELECT;
    public static final RegistryObject<SoundEvent> CHRONICLE_PAUSE;
    public static final RegistryObject<SoundEvent> CHRONICLE_RESTART;
    public static final RegistryObject<SoundEvent> PARADOX;
    public static final RegistryObject<SoundEvent> VRX_OPEN;
    public static final RegistryObject<SoundEvent> VRX_CREATE;
    public static final RegistryObject<SoundEvent> VRX_ERASE;
    public static final RegistryObject<SoundEvent> FUMETSU_AMBIENT;
    public static final RegistryObject<SoundEvent> FUMETSU_HURT;
    public static final RegistryObject<SoundEvent> FUMETSU_SHOOT;
    public static final RegistryObject<SoundEvent> FUMETSU_STORM;

    static {

        MUTEKI = register("muteki_equip");
        NOVEL = register("novelize");
        CHRONICLE_SELECT = register("chronicle_select");
        CHRONICLE_PAUSE = register("chronicle_pause");
        CHRONICLE_RESTART = register("chronicle_restart");
        PARADOX = register("perfect_knockout");
        VRX_OPEN = register("vrx_open");
        VRX_CREATE = register("vrx_create");
        VRX_ERASE = register("vrx_erase");
        FUMETSU_AMBIENT = register("fumetsu_ambient");
        FUMETSU_HURT = register("fumetsu_hurt");
        FUMETSU_SHOOT = register("fumetsu_shoot");
        FUMETSU_STORM = register("fumetsu_storm");
    }

    public static RegistryObject<SoundEvent> register(String name) {
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(identifier(HYPERDAIMC, name)));
    }
}
