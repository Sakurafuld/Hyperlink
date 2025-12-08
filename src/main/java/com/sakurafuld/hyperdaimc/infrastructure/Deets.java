package com.sakurafuld.hyperdaimc.infrastructure;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Supplier;

public class Deets {
    public static final String HYPERDAIMC = "hyperdaimc";
    public static final Logger LOG = LoggerFactory.getLogger("Hyperlink");

    public static final String CURIOS = "curios";
    public static final String KUBE_JS = "kubejs";
    public static final String EMBEDDIUM = "embeddium";
    public static final String MEKANISM = "mekanism";
    public static final String TINKERSCONSTRUCT = "tconstruct";
    public static final String TICEX = "ticex";
    public static final String SLASHBLADE = "slashblade";
    public static final String BOTANIA = "botania";
    public static final String FANTASY_ENDING = "fantasy_ending";
    public static final String ARS_NOUVEAU = "ars_nouveau";
    public static final String IRONS_SPELLS_N_SPELLBOOKS = "irons_spellbooks";
    public static final String PROJECT_E = "projecte";
    public static final String OCULUS = "oculus";

    private Deets() {
    }

    public static boolean require(String modid) {
        return FMLLoader.getLoadingModList().getModFileById(modid) != null;
    }

    public static boolean requireAll(String... modids) {
        return Arrays.stream(modids).allMatch(Deets::require);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation identifier(String nameSpace, String path) {
        return new ResourceLocation(nameSpace, path);
    }

    public static ResourceLocation identifier(String path) {
        return identifier(HYPERDAIMC, path);
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
