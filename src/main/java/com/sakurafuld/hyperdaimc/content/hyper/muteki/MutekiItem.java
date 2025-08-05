package com.sakurafuld.hyperdaimc.content.hyper.muteki;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;

public class MutekiItem extends AbstractGashatItem {

    public MutekiItem(String name, Properties pProperties) {
        super(name, pProperties, 0xEF5030, HyperCommonConfig.ENABLE_MUTEKI);
    }
}
