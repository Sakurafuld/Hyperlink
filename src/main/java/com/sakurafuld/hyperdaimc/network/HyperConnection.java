package com.sakurafuld.hyperdaimc.network;

import com.sakurafuld.hyperdaimc.network.chemical.ClientboundChemicalMutation;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleSyncSave;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChroniclePause;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChronicleRestart;
import com.sakurafuld.hyperdaimc.network.chronicle.VulnerableServerboundChronicleSyncSave;
import com.sakurafuld.hyperdaimc.network.desk.ClientboundDeskMinecraft;
import com.sakurafuld.hyperdaimc.network.desk.ClientboundDeskSyncSave;
import com.sakurafuld.hyperdaimc.network.desk.ServerboundDeskDoneAnimation;
import com.sakurafuld.hyperdaimc.network.desk.ServerboundDeskLockRecipe;
import com.sakurafuld.hyperdaimc.network.materializer.ClientboundMaterializerSyncRecipe;
import com.sakurafuld.hyperdaimc.network.muteki.ServerboundSpecialGameModeSwitch;
import com.sakurafuld.hyperdaimc.network.novel.ClientboundNovelize;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelSound;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelize;
import com.sakurafuld.hyperdaimc.network.novel.VulnerableServerboundNovelize;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxFluid;
import com.sakurafuld.hyperdaimc.network.paradox.ServerboundPerfectKnockout;
import com.sakurafuld.hyperdaimc.network.vrx.*;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class HyperConnection {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE
            = NetworkRegistry.newSimpleChannel(identifier("network"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void initialize() {
        int id = 0;

        INSTANCE.registerMessage(id++, ServerboundSpecialGameModeSwitch.class, ServerboundSpecialGameModeSwitch::encode, ServerboundSpecialGameModeSwitch::decode, ServerboundSpecialGameModeSwitch::handle);

        INSTANCE.registerMessage(id++, ClientboundNovelize.class, ClientboundNovelize::encode, ClientboundNovelize::decode, ClientboundNovelize::handle);
        INSTANCE.registerMessage(id++, ServerboundNovelize.class, ServerboundNovelize::encode, ServerboundNovelize::decode, ServerboundNovelize::handle);
        INSTANCE.registerMessage(id++, ServerboundNovelSound.class, ServerboundNovelSound::encode, ServerboundNovelSound::decode, ServerboundNovelSound::handle);
        INSTANCE.registerMessage(id++, VulnerableServerboundNovelize.class, VulnerableServerboundNovelize::encode, VulnerableServerboundNovelize::decode, VulnerableServerboundNovelize::handle);

        INSTANCE.registerMessage(id++, ClientboundChronicleSyncSave.class, ClientboundChronicleSyncSave::encode, ClientboundChronicleSyncSave::decode, ClientboundChronicleSyncSave::handle);
        INSTANCE.registerMessage(id++, ServerboundChroniclePause.class, ServerboundChroniclePause::encode, ServerboundChroniclePause::decode, ServerboundChroniclePause::handle);
        INSTANCE.registerMessage(id++, ServerboundChronicleRestart.class, ServerboundChronicleRestart::encode, ServerboundChronicleRestart::decode, ServerboundChronicleRestart::handle);
        INSTANCE.registerMessage(id++, VulnerableServerboundChronicleSyncSave.class, VulnerableServerboundChronicleSyncSave::encode, VulnerableServerboundChronicleSyncSave::decode, VulnerableServerboundChronicleSyncSave::handle);

        INSTANCE.registerMessage(id++, ServerboundPerfectKnockout.class, ServerboundPerfectKnockout::encode, ServerboundPerfectKnockout::decode, ServerboundPerfectKnockout::handle);
        INSTANCE.registerMessage(id++, ClientboundParadoxFluid.class, ClientboundParadoxFluid::encode, ClientboundParadoxFluid::decode, ClientboundParadoxFluid::handle);

        INSTANCE.registerMessage(id++, ClientboundVRXSyncSave.class, ClientboundVRXSyncSave::encode, ClientboundVRXSyncSave::decode, ClientboundVRXSyncSave::handle);
        INSTANCE.registerMessage(id++, ClientboundVRXSyncCapability.class, ClientboundVRXSyncCapability::encode, ClientboundVRXSyncCapability::decode, ClientboundVRXSyncCapability::handle);
        INSTANCE.registerMessage(id++, ClientboundVRXSetTooltip.class, ClientboundVRXSetTooltip::encode, ClientboundVRXSetTooltip::decode, ClientboundVRXSetTooltip::handle);
        INSTANCE.registerMessage(id++, ServerboundVRXCloseMenu.class, ServerboundVRXCloseMenu::encode, ServerboundVRXCloseMenu::decode, ServerboundVRXCloseMenu::handle);
        INSTANCE.registerMessage(id++, ServerboundVRXScrollMenu.class, ServerboundVRXScrollMenu::encode, ServerboundVRXScrollMenu::decode, ServerboundVRXScrollMenu::handle);
        INSTANCE.registerMessage(id++, ServerboundVRXSetJeiGhost.class, ServerboundVRXSetJeiGhost::encode, ServerboundVRXSetJeiGhost::decode, ServerboundVRXSetJeiGhost::handle);
        INSTANCE.registerMessage(id++, ServerboundVRXOpenMenu.class, ServerboundVRXOpenMenu::encode, ServerboundVRXOpenMenu::decode, ServerboundVRXOpenMenu::handle);
        INSTANCE.registerMessage(id++, ServerboundVRXErase.class, ServerboundVRXErase::encode, ServerboundVRXErase::decode, ServerboundVRXErase::handle);
        INSTANCE.registerMessage(id++, ServerboundVRXMyself.class, ServerboundVRXMyself::encode, ServerboundVRXMyself::decode, ServerboundVRXMyself::handle);
        INSTANCE.registerMessage(id++, VulnerableServerboundVRXSyncSave.class, VulnerableServerboundVRXSyncSave::encode, VulnerableServerboundVRXSyncSave::decode, VulnerableServerboundVRXSyncSave::handle);
        INSTANCE.registerMessage(id++, VulnerableServerboundVRXOpenMenu.class, VulnerableServerboundVRXOpenMenu::encode, VulnerableServerboundVRXOpenMenu::decode, VulnerableServerboundVRXOpenMenu::handle);
        INSTANCE.registerMessage(id++, VulnerableServerboundVRXEraseCapability.class, VulnerableServerboundVRXEraseCapability::encode, VulnerableServerboundVRXEraseCapability::decode, VulnerableServerboundVRXEraseCapability::handle);

        INSTANCE.registerMessage(id++, ClientboundDeskSyncSave.class, ClientboundDeskSyncSave::encode, ClientboundDeskSyncSave::decode, ClientboundDeskSyncSave::handle);
        INSTANCE.registerMessage(id++, ClientboundDeskMinecraft.class, ClientboundDeskMinecraft::encode, ClientboundDeskMinecraft::decode, ClientboundDeskMinecraft::handle);
        INSTANCE.registerMessage(id++, ServerboundDeskDoneAnimation.class, ServerboundDeskDoneAnimation::encode, ServerboundDeskDoneAnimation::decode, ServerboundDeskDoneAnimation::handle);
        INSTANCE.registerMessage(id++, ServerboundDeskLockRecipe.class, ServerboundDeskLockRecipe::encode, ServerboundDeskLockRecipe::decode, ServerboundDeskLockRecipe::handle);

        INSTANCE.registerMessage(id++, ClientboundChemicalMutation.class, ClientboundChemicalMutation::encode, ClientboundChemicalMutation::decode, ClientboundChemicalMutation::handle);

        INSTANCE.registerMessage(id++, ClientboundMaterializerSyncRecipe.class, ClientboundMaterializerSyncRecipe::encode, ClientboundMaterializerSyncRecipe::decode, ClientboundMaterializerSyncRecipe::handle);
    }
}
