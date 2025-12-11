package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.MixinLevelTickEvent;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.desk.ClientboundDeskMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class DeskHandler {
    public static void minecraftAt(Level level, BlockPos pos, List<ItemStack> ingredients, ItemStack result) {
        if (!level.isClientSide()) {
            DeskSavedData.Entry entry = DeskSavedData.get(level).add(pos, ingredients, result);
            HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundDeskMinecraft(entry));
        }
    }

    @SubscribeEvent
    public static void logIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeskSavedData.get(player.level()).sync2Client(player);
        }
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeskSavedData.get(player.level()).sync2Client(player);
        }
    }

    @SubscribeEvent
    public static void minecrafting(MixinLevelTickEvent event) {
        DeskSavedData.get(event.getLevel()).getEntries().removeIf(entry -> !entry.tick(event.getLevel()));
    }
}
