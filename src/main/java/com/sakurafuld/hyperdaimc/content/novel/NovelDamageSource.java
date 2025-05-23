package com.sakurafuld.hyperdaimc.content.novel;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class NovelDamageSource extends DamageSource {
    @Deprecated
    public NovelDamageSource() {
        super("novel");
        this.bypassArmor().bypassInvul().bypassMagic();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity pLivingEntity) {
        String s = "death.attack." + this.getMsgId() + "." + pLivingEntity.getRandom().nextInt(7);
        return new TranslatableComponent(s, pLivingEntity.getDisplayName());
    }
    public static class Looting extends EntityDamageSource {

        public Looting(LivingEntity pEntity) {
            super("novel", pEntity);
            this.bypassArmor().bypassInvul().bypassMagic();
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity pLivingEntity) {
            String s = "death.attack." + this.getMsgId() + "." + pLivingEntity.getRandom().nextInt(7);
            return new TranslatableComponent(s, pLivingEntity.getDisplayName());
        }
    }
}
