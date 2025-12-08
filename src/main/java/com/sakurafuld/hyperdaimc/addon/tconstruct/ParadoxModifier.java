package com.sakurafuld.hyperdaimc.addon.tconstruct;

import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

public class ParadoxModifier extends NoLevelsModifier {
    @Override
    public @NotNull Component getDisplayName() {
        return Writes.gameOver(super.getDisplayName().getString());
    }

    @Override
    public @NotNull Component getDisplayName(int level) {
        return this.getDisplayName();
    }
}
