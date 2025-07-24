package com.sakurafuld.hyperdaimc.content.hyper.novel;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class NovelDamageSource extends DamageSource {
    private static final ResourceKey<DamageType> TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, identifier(HYPERDAIMC, "novel"));

    public NovelDamageSource(LivingEntity entity) {
        super(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TYPE), entity);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity pLivingEntity) {
        String s = "death.attack." + this.getMsgId() + "." + pLivingEntity.getRandom().nextInt(7);
        return Component.translatable(s, pLivingEntity.getDisplayName());
    }
}
