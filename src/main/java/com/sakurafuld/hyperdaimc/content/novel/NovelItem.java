package com.sakurafuld.hyperdaimc.content.novel;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import mekanism.api.robit.IRobit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.sakurafuld.hyperdaimc.Deets.*;

public class NovelItem extends Item {
    public static Rarity RARITY = Rarity.create("novel", style -> style.withColor(0xCCCCCC));

    public NovelItem(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(new TranslatableComponent("item.lore.novel").withStyle(style -> style.withColor(0x999999)));
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return RARITY;
    }

    public void tryKillRobit(LivingEntity self){
        required(MEKANISM).run(() -> {
            if(!(self instanceof IRobit)) return;
            if(self.isRemoved()) return;
            ++self.deathTime;
            if (self.deathTime == 20 && side().isServer()) {
                self.getLevel().broadcastEntityEvent(self, (byte)60);
                self.remove(Entity.RemovalReason.KILLED);
            }
        });
    }
}
