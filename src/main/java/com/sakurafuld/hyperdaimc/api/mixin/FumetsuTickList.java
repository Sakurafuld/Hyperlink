package com.sakurafuld.hyperdaimc.api.mixin;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FumetsuTickList {
    private Int2ObjectMap<Entity> active = new Int2ObjectLinkedOpenHashMap<>();
    private Int2ObjectMap<Entity> passive = new Int2ObjectLinkedOpenHashMap<>();
    @Nullable
    private Int2ObjectMap<Entity> iterated;

    private void ensureActiveIsNotIterated() {
        if (this.iterated == this.active) {
            this.passive.clear();

            for (Int2ObjectMap.Entry<Entity> entry : Int2ObjectMaps.fastIterable(this.active)) {
                this.passive.put(entry.getIntKey(), entry.getValue());
            }

            Int2ObjectMap<Entity> int2objectmap = this.active;
            this.active = this.passive;
            this.passive = int2objectmap;
        }

    }

    public void add(Entity fumetsu) {
        if (!(fumetsu instanceof IFumetsu)) {
            throw new IllegalArgumentException();
        }
        this.ensureActiveIsNotIterated();
        this.active.put(fumetsu.getId(), fumetsu);
    }

    public void remove(Entity fumetsu) {
        if (!(fumetsu instanceof IFumetsu)) {
            throw new IllegalArgumentException();
        }
        this.ensureActiveIsNotIterated();
        this.active.remove(fumetsu.getId());
    }

    public boolean contains(Entity fumetsu) {
        if (!(fumetsu instanceof IFumetsu)) {
            throw new IllegalArgumentException();
        }
        return this.active.containsKey(fumetsu.getId());
    }

    public void forEach(Consumer<Entity> consumer) {
        if (this.iterated != null) {
            throw new UnsupportedOperationException("Only one concurrent iteration supported");
        } else {
            this.iterated = this.active;

            try {
                for (Entity fumetsu : this.active.values()) {
                    consumer.accept(fumetsu);
                }
            } finally {
                this.iterated = null;
            }

        }
    }
}
