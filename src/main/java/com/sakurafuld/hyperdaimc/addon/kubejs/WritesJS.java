package com.sakurafuld.hyperdaimc.addon.kubejs;

import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.network.chat.Component;

@Info("Hyperlink's text formatter")
public class WritesJS {
    @Info("Returns a gameover gradient component of input")
    public static Component gameOverOf(Object o) {
        if (o instanceof Component c)
            return Writes.gameOver(c.getString());
        else if (o instanceof String s)
            return Writes.gameOver(s);
        else throw new IllegalArgumentException("Argument is not a Component or a String");
    }
}
