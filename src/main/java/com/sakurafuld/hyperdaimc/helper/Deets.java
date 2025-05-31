package com.sakurafuld.hyperdaimc.helper;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Supplier;

public class Deets {
    private Deets() {
    }

    public static final String HYPERDAIMC = "hyperdaimc";
    public static final Logger LOG = LoggerFactory.getLogger(HYPERDAIMC);
    public static final CreativeModeTab TAB = new CreativeModeTab(HYPERDAIMC) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(HyperItems.MUTEKI.get());
        }
    };

    public static final String CURIOS = "curios";
    public static final String EMBEDDIUM = "embeddium";
    public static final String MEKANISM = "mekanism";
    public static final String TINKERSCONSTRUCT = "tconstruct";


    public static Act require(String modid) {
        return FMLLoader.getLoadingModList().getModFileById(modid) != null ? Act.TRUE : Act.FALSE;
    }

    public static Act requireAll(String... modids) {
        return Arrays.stream(modids).allMatch(modid -> FMLLoader.getLoadingModList().getModFileById(modid) != null) ? Act.TRUE : Act.FALSE;
    }

    public static ResourceLocation identifier(String nameSpace, String path) {
        return new ResourceLocation(nameSpace, path);
    }

    public static ResourceLocation[] identifier(String nameSpace, String... paths) {
        ResourceLocation[] resourceLocations = new ResourceLocation[paths.length];
        for (int index = 0; index < paths.length; index++) {
            resourceLocations[index] = new ResourceLocation(nameSpace, paths[index]);
        }
        return resourceLocations;
    }

    public static Act require(ResourceLocation key) {
        return ForgeRegistries.ITEMS.containsKey(key) ? Act.TRUE : Act.FALSE;
    }

    public static Act requiredAny(ResourceLocation... keys) {
        return Arrays.stream(keys).anyMatch(ForgeRegistries.ITEMS::containsKey) ? Act.TRUE : Act.FALSE;
    }

    public static Act requireAll(ResourceLocation... keys) {
        return Arrays.stream(keys).allMatch(ForgeRegistries.ITEMS::containsKey) ? Act.TRUE : Act.FALSE;
    }

    public static LogicalSide side() {
        return EffectiveSide.get();
    }

    public static Act require(LogicalSide side) {
        return side() == side ? Act.TRUE : Act.FALSE;
    }

    public enum Act {
        FALSE,
        TRUE;

        public void run(Runnable runnable) {
            switch (this) {
                case FALSE -> {
                    return;
                }
                case TRUE -> {
                    runnable.run();
                    return;
                }
            }
            throw new IllegalStateException();
        }

        public void runOr(Runnable trueRun, Runnable falseRun) {
            switch (this) {
                case FALSE -> {
                    falseRun.run();
                    return;
                }
                case TRUE -> {
                    trueRun.run();
                    return;
                }
            }
            throw new IllegalStateException();
        }//required()で使うとき、run()はいいけどget()は気をつけなきゃやばい.

        public <T> T get(Supplier<T> supplier) {
            switch (this) {
                case FALSE -> {
                    return null;
                }
                case TRUE -> {
                    return supplier.get();
                }
            }
            throw new IllegalStateException();
        }

        public <T> T getOr(Supplier<T> trueGet, Supplier<T> falseGet) {
            switch (this) {
                case FALSE -> {
                    return falseGet.get();
                }
                case TRUE -> {
                    return trueGet.get();
                }
            }
            throw new IllegalStateException();
        }

        public boolean ready() {
            switch (this) {
                case FALSE -> {
                    return false;
                }
                case TRUE -> {
                    return true;
                }
            }
            throw new IllegalStateException();
        }
    }
}
