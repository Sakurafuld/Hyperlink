package com.sakurafuld.hyperdaimc.infrastructure;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
    public static final String JUST_ENOUGH_ITEMS = "jei";
    public static final String EMI = "emi";

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
}
