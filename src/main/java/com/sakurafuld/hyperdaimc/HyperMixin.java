package com.sakurafuld.hyperdaimc;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

public class HyperMixin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(TINKERSCONSTRUCT) && !require(TINKERSCONSTRUCT).ready()) {
            return false;
        }

        if (require(TICEX).ready()) {
            if (mixinClassName.contains("ModifierDeflectionMixin")) {
                if (FMLLoader.getLoadingModList().getModFileById(TICEX).getMods().get(0).getVersion().getMinorVersion() >= 2) {
                    LOG.info("TICEXMixinCancel");
                    return false;
                } else {
                    LOG.info("TICEXMixinDo");
                }
            }
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
