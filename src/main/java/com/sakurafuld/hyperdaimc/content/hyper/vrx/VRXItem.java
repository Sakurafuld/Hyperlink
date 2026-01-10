package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.infrastructure.item.AbstractGashatItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

public class VRXItem extends AbstractGashatItem {
    private static final Component DESCRIPTION = Component.translatable("tooltip.hyperdaimc.vrx.description").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_CLOSE = Component.translatable("tooltip.hyperdaimc.vrx.description.close").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_ERASE = Component.translatable("tooltip.hyperdaimc.vrx.description.erase").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_PLAYER = Component.translatable("tooltip.hyperdaimc.vrx.description.player").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_JEI = Component.translatable("tooltip.hyperdaimc.vrx.description.jei").withStyle(ChatFormatting.GRAY);
    private static final Component DESCRIPTION_EMI = Component.translatable("tooltip.hyperdaimc.vrx.description.emi").withStyle(ChatFormatting.GRAY);
    private static Component descriptionConfigurable = null;

    private long lastTime = 0;
    private String lastName = VRXHandler.getMake();

    public VRXItem(String name, Properties pProperties) {
        super(name, pProperties, 0xAAFFFF, HyperCommonConfig.ENABLE_VRX);
    }

    @Override
    protected void appendDescription(List<Component> tooltip) {
        tooltip.add(DESCRIPTION);
        tooltip.add(DESCRIPTION_CLOSE);
        tooltip.add(DESCRIPTION_ERASE);
        tooltip.add(DESCRIPTION_PLAYER);
        if (HyperCommonConfig.VRX_JEI.get()) {
            if (require(EMI))
                tooltip.add(DESCRIPTION_EMI);
            else if (require(JUST_ENOUGH_ITEMS))
                tooltip.add(DESCRIPTION_JEI);
        }
        if (descriptionConfigurable == null) {
            String configurables = String.join(", ", VRXRegistry.allNames());
            descriptionConfigurable = Component.translatable("tooltip.hyperdaimc.vrx.description.configurables", Component.literal(configurables).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)).withStyle(ChatFormatting.WHITE);
        }
        tooltip.add(descriptionConfigurable);
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!this.enabled.get()) return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
        return !pPlayer.isCreative();
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (!this.enabled.get()) return super.getName(pStack);
        if (Util.getMillis() - this.lastTime > 100)
            this.lastName = VRXHandler.getMake();
        this.lastTime = Util.getMillis();
        return super.getName(pStack).copy().append(" (" + this.lastName + ")");
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        if (!this.enabled.get()) return super.getUseAnimation(pStack);
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        if (!this.enabled.get()) return super.getUseDuration(pStack);
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!this.enabled.get()) return super.use(pLevel, pPlayer, pUsedHand);

        if (pUsedHand == InteractionHand.MAIN_HAND && pPlayer.isShiftKeyDown()) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
        } else return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!this.enabled.get()) return super.finishUsingItem(pStack, pLevel, pLivingEntity);

        if (pLivingEntity instanceof ServerPlayer player) {
            Pair<List<Direction>, List<VRXType>> pair = VRXMenu.Canvas.getAvailables(player);
            VRXMenu.Canvas canvas = VRXMenu.Canvas.entity(player.getId(), null, pair.getFirst(), pair.getSecond());
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return player.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    return new VRXMenu(pContainerId, pPlayerInventory, canvas);
                }
            }, canvas::write);

            player.playNotifySound(HyperSounds.VRX_OPEN.get(), SoundSource.PLAYERS, 0.5f, 0.75f);
        }

        return new ItemStack(HyperItems.VRX.get());
    }
}
