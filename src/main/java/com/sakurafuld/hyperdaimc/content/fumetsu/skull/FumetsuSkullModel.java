package com.sakurafuld.hyperdaimc.content.fumetsu.skull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FumetsuSkullModel extends Model {
    private final ModelPart root;
    private final ModelPart head;

    public FumetsuSkullModel(ModelPart pRoot) {
        super(Renders.Type::additiveEntityTranslucent);
        this.root = pRoot;
        this.head = pRoot.getChild("head");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4, -8, -4, 8, 8, 8), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setup(float yaw, float pitch) {
        this.head.yRot = (float) Math.toRadians(Mth.wrapDegrees(yaw - 180));
        this.head.xRot = (float) Math.toRadians(pitch);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }
}
