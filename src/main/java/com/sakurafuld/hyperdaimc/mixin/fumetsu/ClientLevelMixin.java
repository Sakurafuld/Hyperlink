package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.mixin.FumetsuTickList;
import com.sakurafuld.hyperdaimc.api.mixin.IClientLevelFumetsu;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
@OnlyIn(Dist.CLIENT)
public abstract class ClientLevelMixin implements IClientLevelFumetsu {

    @Unique
    private final FumetsuTickList fumetsuTickList = new FumetsuTickList();

    @Override
    public FumetsuTickList fumetsuTickList() {
        return this.fumetsuTickList;
    }
}
