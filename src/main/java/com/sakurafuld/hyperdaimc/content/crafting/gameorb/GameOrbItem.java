package com.sakurafuld.hyperdaimc.content.crafting.gameorb;

import com.sakurafuld.hyperdaimc.helper.Writes;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class GameOrbItem extends Item {
    public GameOrbItem(Properties pProperties) {
        super(pProperties.rarity(Rarity.create("game_over", style -> style.withColor(0x4444444))).fireResistant());
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Writes.gameOver(super.getName(pStack).getString());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
//        pTooltipComponents.add(new TranslatableComponent("tooltip.hyperdaimc.game_orb_0").withStyle(ChatFormatting.GRAY));
//        pTooltipComponents.add(new TranslatableComponent("tooltip.hyperdaimc.game_orb_1").withStyle(ChatFormatting.GRAY));
//        pTooltipComponents.add(new TranslatableComponent("tooltip.hyperdaimc.game_orb_2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            private final GameOrbRenderer renderer = new GameOrbRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return this.renderer;
            }
        });
    }
}
