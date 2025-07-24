package com.sakurafuld.hyperdaimc.compat.tconstruct;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Writes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.novel.ClientboundNovelize;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.TinkerHooks;
import slimeknights.tconstruct.library.modifiers.hook.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.NamespacedNBT;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class NovelModifier extends NoLevelsModifier implements MeleeHitModifierHook, ProjectileHitModifierHook {
    @Override
    public @NotNull Component getDisplayName() {
        return Writes.gameOver(super.getDisplayName().getString());
    }

    @Override
    public @NotNull Component getDisplayName(int level) {
        return this.getDisplayName();
    }

    @Override
    protected void registerHooks(ModifierHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, TinkerHooks.MELEE_HIT, TinkerHooks.PROJECTILE_HIT);
    }

    @Override
    public float beforeMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull ToolAttackContext context, float damage, float baseKnockback, float knockback) {
        require(LogicalSide.SERVER).run(() -> {
            if (tool.hasTag(TinkerTags.Items.MELEE_PRIMARY)) {
                LivingEntity attacker = context.getAttacker();
                Entity target = context.getTarget();
                if (NovelHandler.PREDICATE.test(target)) {
                    NovelHandler.novelize(attacker, target, false);
                    HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(attacker.getLevel()::dimension), new ClientboundNovelize(attacker.getId(), target.getId(), 1));
                    if (attacker.getLevel() instanceof ServerLevel serverLevel) {
                        NovelHandler.playSound(serverLevel, target.position());
                    }
                }
            }
        });

        return MeleeHitModifierHook.super.beforeMeleeHit(tool, modifier, context, damage, baseKnockback, knockback);
    }

    @Override
    public boolean onProjectileHitEntity(@NotNull ModifierNBT modifiers, @NotNull NamespacedNBT persistentData, @NotNull ModifierEntry modifier, @NotNull Projectile projectile, @NotNull EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        require(LogicalSide.SERVER).run(() -> {
            if (attacker != null) {
                Entity entity = hit.getEntity();
                if (!NovelHandler.novelized(entity)) {
                    NovelHandler.novelize(attacker, entity, false);
                    HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(attacker.getLevel()::dimension), new ClientboundNovelize(attacker.getId(), entity.getId(), 1));
                    if (attacker.getLevel() instanceof ServerLevel serverLevel) {
                        NovelHandler.playSound(serverLevel, entity.position());
                    }
                }
            }
        });

        return ProjectileHitModifierHook.super.onProjectileHitEntity(modifiers, persistentData, modifier, projectile, hit, attacker, target);
    }
}
