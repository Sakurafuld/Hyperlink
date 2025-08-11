package com.sakurafuld.hyperdaimc.mixin.novel;

import flashfur.omnimobs.coremod.MixinRemoverClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Pseudo
@Mixin(MixinRemoverClassVisitor.class)
public abstract class MixinRemoverClassVisitorMixin extends ClassVisitor {
    protected MixinRemoverClassVisitorMixin(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Inject(method = "visitMethod", at = @At("HEAD"), cancellable = true, remap = false)
    private void visitMethod(int access, String name, String descriptor, String signature, String[] exceptions, CallbackInfoReturnable<MethodVisitor> cir) {
        if (name.contains(HYPERDAIMC)) {
            cir.setReturnValue(super.visitMethod(access, name, descriptor, signature, exceptions));
        }
    }
}
