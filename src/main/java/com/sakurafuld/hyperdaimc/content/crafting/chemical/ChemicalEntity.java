package com.sakurafuld.hyperdaimc.content.crafting.chemical;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chemical.ClientboundChemicalMutation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.PacketDistributor;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ChemicalEntity extends ThrownPotion {
    public ChemicalEntity(EntityType<? extends ThrownPotion> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void setup(Player player) {
        this.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        this.setOwner(player);
    }

    @Override
    protected Item getDefaultItem() {
        return HyperItems.CHEMICAL_MAX.get();
    }

    @Override
    protected void onHit(HitResult pResult) {
        if (pResult.getType() != HitResult.Type.MISS) {
            this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
        }

        require(LogicalSide.SERVER).run(() -> this.level().getEntities(this, this.getBoundingBox().inflate(2, 1, 2), EntitySelector.LIVING_ENTITY_STILL_ALIVE).stream()
                .map(LivingEntity.class::cast)
                .filter(entity -> !entity.getPersistentData().contains(ChemicalHandler.TAG_MUTATION))
                .forEach(entity -> {
                    if (entity instanceof Zombie) {
                        entity.playSound(HyperSounds.CHEMICAL_MAXIMIZATION.get(), 1, 1);
                        entity.getPersistentData().putInt(ChemicalHandler.TAG_MUTATION, 0);
                        HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientboundChemicalMutation(entity.getId()));
                    } else {
                        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 300 + this.random.nextInt(50), 1), this.getOwner());
                        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 300 + this.random.nextInt(50), 1), this.getOwner());
                        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200 + this.random.nextInt(200), 0), this.getOwner());
                        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100 + this.random.nextInt(100), 0), this.getOwner());
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50 + this.random.nextInt(50), 0), this.getOwner());
                        entity.setSecondsOnFire(5 + this.random.nextInt(3));
                    }
                }));

        this.level().levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, this.blockPosition(), 0x805080);
        this.discard();
    }
}
