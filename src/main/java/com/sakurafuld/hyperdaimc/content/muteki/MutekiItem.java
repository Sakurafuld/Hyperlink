package com.sakurafuld.hyperdaimc.content.muteki;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;

public class MutekiItem extends AbstractGashatItem {

    public MutekiItem(String name, Properties pProperties) {
        super(name, pProperties, 0xEF5030, HyperServerConfig.ENABLE_MUTEKI);
    }
}
