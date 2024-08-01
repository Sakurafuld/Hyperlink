package com.sakurafuld.hyperdaimc.content.novel;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

public class NovelDamageSource extends EntityDamageSource {
    public NovelDamageSource(LivingEntity pEntity) {
        super("novel", pEntity);
    }
    @Override
    public Component getLocalizedDeathMessage(LivingEntity pLivingEntity) {
        String s = "death.attack." + this.getMsgId() + "." + pLivingEntity.getRandom().nextInt(7);
        return new TranslatableComponent(s, pLivingEntity.getDisplayName());
    }
}
