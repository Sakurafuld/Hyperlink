package com.sakurafuld.hyperdaimc.api.content;

import com.sakurafuld.hyperdaimc.helper.Writes;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public abstract class AbstractGashatItem extends Item {
    protected final Component tooltip;
    protected final Rarity rarity;
    protected final Supplier<Boolean> enabled;
    private final ResourceLocation model;

    public AbstractGashatItem(String name, Properties pProperties, int rarity, Supplier<Boolean> enabled) {
        super(pProperties.stacksTo(1).fireResistant());
        this.tooltip = Component.translatable("tooltip.hyperdaimc." + name);
        this.rarity = Rarity.create(name, style -> style.withColor(rarity));
        this.enabled = enabled;
        this.model = identifier("special/" + name);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (this.enabled.get()) {
            pTooltipComponents.add(Writes.gameOver(this.tooltip.getString()));
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        if (this.enabled.get()) {
            return this.rarity;
        } else {
            return super.getRarity(pStack);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GashatItemRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer == null ? this.renderer = new GashatItemRenderer(AbstractGashatItem.this.model) : this.renderer;
            }
        });
    }
}
