package com.sakurafuld.hyperdaimc;

import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.ModSounds;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.SlotTypeMessage;

import static com.sakurafuld.hyperdaimc.Deets.*;

@Mod(HYPERDAIMC)
public class HyperDaiMC {
    public HyperDaiMC(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        /*required(LogicalSide.CLIENT).run(()-> {
            bus.addListener(this::registerTextures);
        });*/
        bus.addListener(this::commonSetup);
        bus.addListener(this::IMC);
        ModItems.REGISTRY.register(bus);
        ModSounds.REGISTRY.register(bus);
    }

    private void commonSetup(FMLCommonSetupEvent event){
        event.enqueueWork(PacketHandler::initialize);
    }
    private void IMC(InterModEnqueueEvent event) {
        InterModComms.sendTo(CURIOS, SlotTypeMessage.REGISTER_TYPE, ()-> new SlotTypeMessage.Builder("maximum").icon(identifier(HYPERDAIMC, "item/empty_maximum_slot")).priority(Integer.MIN_VALUE).build());
    }
    /*@OnlyIn(Dist.CLIENT)
    private static void registerTextures(TextureStitchEvent.Pre event) {
        event.addSprite(identifier(HYPERDAIMC, "item/empty_maximum_slot"));
    }*/
    @Mod.EventBusSubscriber(modid = HYPERDAIMC, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientHandler {
        @SubscribeEvent
        public static void registerTextures(TextureStitchEvent.Pre event) {
            event.addSprite(identifier(HYPERDAIMC, "item/empty_maximum_slot"));
        }
    }
}
