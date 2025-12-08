package com.sakurafuld.hyperdaimc.addon.tconstruct;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

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
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.PROJECTILE_HIT);
    }

    @Override
    public float beforeMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull ToolAttackContext context, float damage, float baseKnockback, float knockback) {
        if (context.getLevel() instanceof ServerLevel serverLevel) {
            LivingEntity attacker = context.getAttacker();
            if (tool.hasTag(TinkerTags.Items.MELEE_PRIMARY)) {
                Entity target = context.getTarget();
                if (NovelHandler.PREDICATE.test(target)) {
                    if (attacker instanceof ServerPlayer player)
                        NovelHandler.captureAndTransfer(player, () -> NovelHandler.novelize(player, target, true));
                    else NovelHandler.novelize(attacker, target, true);
                    NovelHandler.playSound(serverLevel, target.position());
                }
            }
        }

        return MeleeHitModifierHook.super.beforeMeleeHit(tool, modifier, context, damage, baseKnockback, knockback);
    }

    @Override
    public boolean onProjectileHitEntity(@NotNull ModifierNBT modifiers, @NotNull ModDataNBT persistentData, @NotNull ModifierEntry modifier, @NotNull Projectile projectile, @NotNull EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        if (attacker != null && attacker.level() instanceof ServerLevel serverLevel) {
            Entity entity = hit.getEntity();
            if (NovelHandler.PREDICATE.test(entity)) {
                if (attacker instanceof ServerPlayer player) {
                    NovelHandler.captureAndTransfer(player, () -> NovelHandler.novelize(player, entity, true));
                    NovelHandler.playSound(serverLevel, player, entity.position());
                } else {
                    NovelHandler.novelize(attacker, entity, true);
                    NovelHandler.playSound(serverLevel, entity.position());
                }
            }
        }

        return ProjectileHitModifierHook.super.onProjectileHitEntity(modifiers, persistentData, modifier, projectile, hit, attacker, target);
    }
}
