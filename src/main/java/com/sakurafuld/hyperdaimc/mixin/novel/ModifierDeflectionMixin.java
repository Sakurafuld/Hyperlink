package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.compat.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.novel.ClientboundNovelize;
import moffy.ticex.modifier.ModifierDeflection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

@Pseudo
@Mixin(ModifierDeflection.class)
public abstract class ModifierDeflectionMixin {
    @Inject(method = "getMeleeDamage", at = @At(value = "INVOKE", target = "Lmoffy/ticex/entity/FakeLivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), remap = false)
    private void getMeleeDamageNovel(IToolStackView tool, ModifierEntry modifierEntry, ToolAttackContext context, float baseDamage, float damage, CallbackInfoReturnable<Float> cir) {
        require(LogicalSide.SERVER).run(() -> {
            if (tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0 && tool.hasTag(TinkerTags.Items.MELEE_PRIMARY)) {
                LivingEntity attacker = context.getAttacker();
                Entity target = context.getTarget();
                if (NovelHandler.PREDICATE.test(target)) {
                    NovelHandler.novelize(attacker, target, false);
                    PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(attacker.level()::dimension), new ClientboundNovelize(attacker.getId(), target.getId(), 1));
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        NovelHandler.playSound(serverLevel, target.position());
                    }

                }
            }
        });
        LOG.info("TiCExNovelize");
    }
}
