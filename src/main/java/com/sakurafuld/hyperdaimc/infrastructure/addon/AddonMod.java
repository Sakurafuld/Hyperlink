package com.sakurafuld.hyperdaimc.infrastructure.addon;

import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AddonMod {
    Type TYPE = Type.getType(AddonMod.class);

    String[] value();
}
