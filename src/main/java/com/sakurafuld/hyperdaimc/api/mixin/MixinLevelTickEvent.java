package com.sakurafuld.hyperdaimc.api.mixin;

import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class MixinLevelTickEvent extends Event {
    private final Level level;

    public MixinLevelTickEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return this.level;
    }
}
