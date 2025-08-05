package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class VRXItem extends AbstractGashatItem {
    private long lastTime = 0;
    private String lastName = VRXHandler.getMake();

    public VRXItem(String name, Properties pProperties) {
        super(name, pProperties, 0xAAFFFF, HyperCommonConfig.ENABLE_VRX);
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        if (!this.enabled.get()) {
            return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
        }
        return !pPlayer.isCreative();
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (!this.enabled.get()) {
            return super.getName(pStack);
        }
        if (Util.getMillis() - this.lastTime > 100) {
            this.lastName = VRXHandler.getMake();
        }
        this.lastTime = Util.getMillis();
        return super.getName(pStack).copy().append(" (" + this.lastName + ")");
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        if (!this.enabled.get()) {
            return super.getUseAnimation(pStack);
        }
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        if (!this.enabled.get()) {
            return super.getUseDuration(pStack);
        }

        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!this.enabled.get()) {
            return super.use(pLevel, pPlayer, pUsedHand);
        }

        if (pUsedHand == InteractionHand.MAIN_HAND && pPlayer.isShiftKeyDown()) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
        } else {
            return super.use(pLevel, pPlayer, pUsedHand);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!this.enabled.get()) {
            return super.finishUsingItem(pStack, pLevel, pLivingEntity);
        }

        if (pLivingEntity instanceof ServerPlayer player) {
            NetworkHooks.openGui(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return player.getDisplayName();
                }

                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                    return new VRXMenu(pContainerId, pPlayerInventory, Pair.of(Pair.of(null, player.getId()), null));
                }
            }, buf -> VRXMenu.parse(buf, Pair.of(Pair.of(null, player.getId()), null)));
        }

        return new ItemStack(HyperItems.VRX.get());
    }
}
