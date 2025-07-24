package com.sakurafuld.hyperdaimc.content.crafting.chemical;

import com.sakurafuld.hyperdaimc.content.HyperEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChemicalItem extends ThrowablePotionItem {
    public ChemicalItem(Properties pProperties) {
        super(pProperties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide()) {
            ChemicalEntity chemical = new ChemicalEntity(HyperEntities.CHEMICAL_MAX.get(), pLevel);
            chemical.setup(pPlayer);
            chemical.setItem(stack);
            chemical.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), -20, 0.5f, 1);
            pLevel.addFreshEntity(chemical);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5f, 0.01f / (pLevel.getRandom().nextFloat() * 0.4f + 0.8f));
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.NONE;
    }

    @Override
    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems) {
        if (this.allowdedIn(pGroup)) {
            pItems.add(this.getDefaultInstance());
        }
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        return this.getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(new TranslatableComponent("tooltip.hyperdaimc.chemical_max").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }
}
