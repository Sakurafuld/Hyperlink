package com.sakurafuld.hyperdaimc.infrastructure.item;

import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeI18n;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public abstract class AbstractGashatItem extends Item {
    private static final Component SHOW_DESCRIPTION = Component.translatable("tooltip.hyperdaimc.show_description").withStyle(ChatFormatting.GRAY);
    protected final String tooltip;
    protected final Rarity rarity;
    protected final Supplier<Boolean> enabled;
    private final ResourceLocation model;

    public AbstractGashatItem(String name, Properties pProperties, int rarity, Supplier<Boolean> enabled) {
        super(pProperties.stacksTo(1).fireResistant());
        this.tooltip = "tooltip.hyperdaimc." + name;
        this.rarity = Rarity.create(name, style -> style.withColor(rarity));
        this.enabled = enabled;
        this.model = identifier("special/" + name);
    }

    protected void appendDescription(List<Component> tooltip) {
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (this.enabled.get()) {
            pTooltipComponents.add(Writes.gameOver(ForgeI18n.getPattern(this.tooltip)));
            if (pLevel == null || !pLevel.isClientSide() || Screen.hasShiftDown())
                this.appendDescription(pTooltipComponents);
            else pTooltipComponents.add(SHOW_DESCRIPTION);
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        if (this.enabled.get()) return this.rarity;
        else return super.getRarity(pStack);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GashatItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer == null ? this.renderer = new GashatItemRenderer(AbstractGashatItem.this.model, AbstractGashatItem.this.enabled) : this.renderer;
            }
        });
    }
}
