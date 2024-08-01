package com.sakurafuld.hyperdaimc.network;

import com.sakurafuld.hyperdaimc.Deets;
import com.sakurafuld.hyperdaimc.network.novel.C2SNovelKill;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE
            = NetworkRegistry.newSimpleChannel(new ResourceLocation(Deets.HYPERDAIMC, "main"), ()-> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
     public static void initialize(){
         int id = 0;
         INSTANCE.registerMessage(id++, C2SNovelKill.class, C2SNovelKill::encode, C2SNovelKill::decode, C2SNovelKill::handle);
     }
}
