package com.sakurafuld.hyperdaimc.content.hyper.fumetsu;

import net.minecraft.client.model.HierarchicalModel;
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
public class FumetsuEntityModel extends HierarchicalModel<FumetsuEntity> {
    private final ModelPart root;
    private final ModelPart centerHead;
    private final ModelPart rightHead;
    private final ModelPart leftHead;
    private final ModelPart ribcage;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    private final ModelPart tail;

    public FumetsuEntityModel(ModelPart pRoot) {
        this.root = pRoot;
        ModelPart shoulders = pRoot.getChild("shoulders");
        this.rightWing = shoulders.getChild("right_wing");
        this.leftWing = shoulders.getChild("left_wing");
        this.ribcage = pRoot.getChild("ribcage");
        this.tail = pRoot.getChild("tail");
        this.centerHead = pRoot.getChild("center_head");
        this.rightHead = pRoot.getChild("right_head");
        this.leftHead = pRoot.getChild("left_head");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition shoulders = partdefinition.addOrReplaceChild("shoulders", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-10, 3.9F, -0.5F, 20, 3, 3),
                PartPose.ZERO);

        shoulders.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(88, 26).mirror().addBox(-5.5F, -5.25f, -3F, 11, 24, 2).mirror(false), PartPose.offsetAndRotation(-4.8173F, 8.5811F, 5.2F, 0.2F, 0.05F, 0.36F));
        shoulders.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(88, 0).addBox(-5.5F, -5.25f, -3F, 11, 24, 2), PartPose.offsetAndRotation(4.8173F, 8.5811F, 5.2F, 0.2F, -0.05F, -0.36F));

        float offset = 0.20420352F;
        partdefinition.addOrReplaceChild("ribcage", CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(0, 0, 0, 3, 10, 3)
                        .texOffs(24, 22)
                        .addBox(-4, 1.5F, 0.5F, 11, 2, 2)
                        .texOffs(24, 26)
                        .addBox(-4, 4, 0.5F, 11, 2, 2)
                        .texOffs(24, 30)
                        .addBox(-4, 6.5F, 0.5F, 11, 2, 2),
                PartPose.offsetAndRotation(-1.5F, 6.9F, -0.5F, offset, 0, 0));

        partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(12, 22)
                        .addBox(0, 0, 0, 3, 6, 3),
                PartPose.offsetAndRotation(-1.5F, 6.9F + Mth.cos(offset) * 10, -0.5F + Mth.sin(offset) * 10, 0.83252203F, 0, 0));

        PartDefinition centerHead = partdefinition.addOrReplaceChild("center_head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4, -4, -4, 8, 8, 8)
                        .texOffs(50, 12)
                        .addBox(-2.5f, 1, -5f, 5, 5, 2),
                PartPose.ZERO);

        centerHead.addOrReplaceChild("center_head_mirror", CubeListBuilder.create().texOffs(6, 35).addBox(-1.5F, -1.5F, -0.5F, 3, 3, 0), PartPose.offsetAndRotation(0, -4, -3.6F, 0.1047F, 0, 0));
        centerHead.addOrReplaceChild("left_head_mirror", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(1, -9, -1, 0, 9, 3).mirror(false), PartPose.offsetAndRotation(-0.1F, -1, -3.3F, 0.3894F, 0.1778F, -0.017F));
        centerHead.addOrReplaceChild("right_head_mirror", CubeListBuilder.create().texOffs(0, 32).addBox(-1, -9, -1, 0, 9, 3), PartPose.offsetAndRotation(0.1F, -1, -3.3F, 0.3894F, -0.1778F, 0.017F));

        centerHead.addOrReplaceChild("right_circlet", CubeListBuilder.create().texOffs(50, 25).mirror().addBox(-4.5F, -1, 0, 9, 4, 0).mirror(false), PartPose.offsetAndRotation(-3.6126F, -5.8F, -2.1548F, 0.2912F, 0.3297F, 0.7469F));
        centerHead.addOrReplaceChild("left_circlet", CubeListBuilder.create().texOffs(50, 19).addBox(-4.5F, -3, 0, 9, 6, 0), PartPose.offsetAndRotation(4.1126F, -6.5F, -2.1548F, 0.0808F, -0.4293F, -0.1922F));

        centerHead.addOrReplaceChild("right_hair_0", CubeListBuilder.create().texOffs(80, 0).addBox(-0.5044F, -1.5658F, -0.5F, 1, 5, 1), PartPose.offsetAndRotation(-2.9956F, -4.9342F, -3.6F, 0.0838F, 0.0805F, -0.6295F));
        centerHead.addOrReplaceChild("right_hair_1", CubeListBuilder.create().texOffs(80, 0).addBox(-0.5044F, -2.5658F, -0.5F, 1, 7, 1), PartPose.offsetAndRotation(-4.3956F, -3.8342F, -4.7F, -0.3696F, 0.2943F, 0.0842F));
        centerHead.addOrReplaceChild("right_hair_2", CubeListBuilder.create().texOffs(80, 0).addBox(-0.4543F, -2.6158F, -0.45F, 1, 8, 1), PartPose.offsetAndRotation(-4.9957F, 2.5658F, -6.2F, 0.0201F, 0.2646F, -0.0903F));
        centerHead.addOrReplaceChild("right_hair_3", CubeListBuilder.create().texOffs(80, 9).addBox(-0.4043F, -2.5658F, -0.5F, 1, 4, 1), PartPose.offsetAndRotation(-4.5957F, 10.3658F, -5.9F, 0.0553F, 0.2596F, 0.0451F));

        centerHead.addOrReplaceChild("left_hair_0", CubeListBuilder.create().texOffs(84, 0).mirror().addBox(-0.4956F, -1.5658F, -0.5F, 1, 5, 1).mirror(false), PartPose.offsetAndRotation(2.9956F, -4.9342F, -3.6F, 0.0838F, -0.0805F, 0.6295F));
        centerHead.addOrReplaceChild("left_hair_1", CubeListBuilder.create().texOffs(84, 0).mirror().addBox(-0.4956F, -2.5658F, -0.5F, 1, 7, 1).mirror(false), PartPose.offsetAndRotation(4.3956F, -3.8342F, -4.7F, -0.3696F, -0.2943F, -0.0842F));
        centerHead.addOrReplaceChild("left_hair_2", CubeListBuilder.create().texOffs(84, 0).mirror().addBox(-0.5457F, -2.6158F, -0.45F, 1, 8, 1).mirror(false), PartPose.offsetAndRotation(4.9957F, 2.5658F, -6.2F, 0.0201F, -0.2646F, 0.0903F));
        centerHead.addOrReplaceChild("left_hair_3", CubeListBuilder.create().texOffs(84, 9).mirror().addBox(-0.5957F, -2.5658F, -0.5F, 1, 4, 1).mirror(false), PartPose.offsetAndRotation(4.5957F, 10.3658F, -5.9F, 0.0553F, -0.2596F, -0.0451F));

        centerHead.addOrReplaceChild("right_back_hair_0", CubeListBuilder.create().texOffs(80, 0).addBox(-7.0422F, -5.5297F, -2.9445F, 1, 7, 1), PartPose.offsetAndRotation(-5.6567F, 0.8319998f, 5.0458F, 0.7948F, -0.0912F, 1.9278F));
        centerHead.addOrReplaceChild("right_back_hair_1", CubeListBuilder.create().texOffs(80, 0).addBox(-4.5278F, -5.8722F, -1.8211F, 1, 5, 1), PartPose.offsetAndRotation(-5.6567F, 0.8319998f, 5.0458F, 0.9954F, 0.7322F, 1.2427F));
        centerHead.addOrReplaceChild("right_back_hair_2", CubeListBuilder.create().texOffs(80, 0).addBox(-3.2459F, -3.4217F, -1.4656F, 1, 8, 1), PartPose.offsetAndRotation(-5.6567F, 0.8319998f, 5.0458F, 0.7614F, 1.2151F, 0.6661F));
        centerHead.addOrReplaceChild("right_back_hair_3", CubeListBuilder.create().texOffs(80, 14).addBox(-4.185F, 3.845F, 0.0297F, 1, 6, 1), PartPose.offsetAndRotation(-5.6567F, 0.8319998f, 5.0458F, -0.1087F, 1.2371F, 0.0679F));

        centerHead.addOrReplaceChild("left_back_hair_0", CubeListBuilder.create().texOffs(84, 0).mirror().addBox(6.0422F, -5.5297F, -2.9445F, 1, 7, 1).mirror(false), PartPose.offsetAndRotation(5.6567F, 0.8319998f, 5.0458F, 0.7948F, 0.0912F, -1.9278F));
        centerHead.addOrReplaceChild("left_back_hair_1", CubeListBuilder.create().texOffs(84, 0).mirror().addBox(3.5278F, -5.8722F, -1.8211F, 1, 5, 1).mirror(false), PartPose.offsetAndRotation(5.6567F, 0.8319998f, 5.0458F, 0.9954F, -0.7322F, -1.2427F));
        centerHead.addOrReplaceChild("left_back_hair_2", CubeListBuilder.create().texOffs(84, 0).mirror().addBox(2.2459F, -3.4217F, -1.4656F, 1, 8, 1).mirror(false), PartPose.offsetAndRotation(5.6567F, 0.8319998f, 5.0458F, 0.7614F, -1.2151F, -0.6661F));
        centerHead.addOrReplaceChild("left_back_hair_3", CubeListBuilder.create().texOffs(84, 14).mirror().addBox(3.185F, 3.845F, 0.0297F, 1, 6, 1).mirror(false), PartPose.offsetAndRotation(5.6567F, 0.8319998f, 5.0458F, -0.1087F, -1.2371F, -0.0679F));

        PartDefinition rightHead = partdefinition.addOrReplaceChild("right_head", CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4, -4, -4, 6, 6, 6),
                PartPose.offset(-8, 4, 0));
        rightHead = rightHead.addOrReplaceChild("right_head_reset", CubeListBuilder.create(), PartPose.offset(8, -4, 0));

        rightHead.addOrReplaceChild("right_star_0", CubeListBuilder.create().texOffs(49, 33).addBox(-2, -2, -0.5F, 5, 5, 1), PartPose.offsetAndRotation(-11, -2, -1, 0, 0, 0.3054F));
        rightHead.addOrReplaceChild("right_star_1", CubeListBuilder.create().texOffs(49, 39).addBox(-2, -0.4688F, -0.1165F, 4, 1, 0), PartPose.offsetAndRotation(-11.3F, -3.883F, -1.05F, -0.0327F, -0.0681F, 1.7195F));
        rightHead.addOrReplaceChild("right_star_2", CubeListBuilder.create().texOffs(49, 39).addBox(-2, -0.4688F, 0.1165F, 4, 1, 0), PartPose.offsetAndRotation(-11.3F, -3.883F, -1.05F, 0.4909F, -0.0681F, 1.7195F));
        rightHead.addOrReplaceChild("right_star_4", CubeListBuilder.create().texOffs(49, 40).addBox(-3.5F, -0.4688F, 0.1165F, 9, 1, 0), PartPose.offsetAndRotation(-12.5307F, -3.7773F, -1.05F, 0.2618F, 0, -1.6581F));
        rightHead.addOrReplaceChild("right_star_3", CubeListBuilder.create().texOffs(49, 40).addBox(-3.5F, -0.4688F, -0.1165F, 9, 1, 0), PartPose.offsetAndRotation(-12.5307F, -3.7773F, -1.05F, -0.2618F, 0, -1.6581F));
        rightHead.addOrReplaceChild("right_star_5", CubeListBuilder.create().texOffs(61, 32).addBox(1.5F, -0.4688F, 0.0665F, 1, 8, 0), PartPose.offsetAndRotation(-9.2307F, -6.9772F, -1.05F, 0.1309F, 0, 1.5272F));
        rightHead.addOrReplaceChild("right_star_6", CubeListBuilder.create().texOffs(61, 32).addBox(1.5F, -0.4688F, -0.0665F, 1, 8, 0), PartPose.offsetAndRotation(-9.2307F, -6.9772F, -1.05F, -0.1309F, 0, 1.5272F));
        rightHead.addOrReplaceChild("right_star_7", CubeListBuilder.create().texOffs(63, 32).addBox(-0.5F, -3.9312F, -0.5223F, 1, 8, 0), PartPose.offsetAndRotation(-12.7325F, -6.8268F, -1.05F, -0.1309F, 0, -1.3526F));
        rightHead.addOrReplaceChild("right_star_8", CubeListBuilder.create().texOffs(63, 32).addBox(-0.5F, -3.9312F, 0.5223F, 1, 8, 0), PartPose.offsetAndRotation(-12.7325F, -6.8268F, -1.05F, 0.1309F, 0, -1.3526F));

        PartDefinition leftHead = partdefinition.addOrReplaceChild("left_head", CubeListBuilder.create()
                        .texOffs(56, 0)
                        .addBox(-4, -4, -4, 6, 6, 6),
                PartPose.offset(10, 4, 0));
        leftHead = leftHead.addOrReplaceChild("left_head_reset", CubeListBuilder.create(), PartPose.offset(-10, -4, 0));

        leftHead.addOrReplaceChild("left_star_2", CubeListBuilder.create().texOffs(24, 43).addBox(-5.5F, -0.5F, -0.5F, 11, 1, 1), PartPose.offsetAndRotation(11.2336F, -2.4606F, -1, 0, 0, 0.3578F));
        leftHead.addOrReplaceChild("left_star_1", CubeListBuilder.create().texOffs(24, 35).addBox(-4.85F, -2.275F, -0.5F, 5, 7, 1), PartPose.offsetAndRotation(16.5F, -6, -1, 0, 0, 0.1963F));
        leftHead.addOrReplaceChild("left_star_0", CubeListBuilder.create().texOffs(12, 35).addBox(-3.5F, -2, -0.5F, 5, 8, 1), PartPose.offsetAndRotation(12.5F, -3, -1, 0, 0, 0.1963F));


        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void prepareMobModel(FumetsuEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
        this.setupHeadRotation(pEntity, this.rightHead, 0, pPartialTick);
        this.setupHeadRotation(pEntity, this.leftHead, 1, pPartialTick);
    }

    @Override
    public void setupAnim(FumetsuEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        float rib = Mth.cos(pAgeInTicks * 0.1F);
        this.ribcage.xRot = (0.065F + 0.05F * rib) * (float) Math.PI;
        this.tail.setPos(-1.5F, 6.9F + Mth.cos(this.ribcage.xRot) * 10, -0.5F + Mth.sin(this.ribcage.xRot) * 10);
        this.tail.xRot = (0.265F + 0.1F * rib) * (float) Math.PI;
        this.centerHead.yRot = (float) Math.toRadians(pNetHeadYaw);
        this.centerHead.xRot = (float) Math.toRadians(pHeadPitch);

        float wing = Mth.cos(pAgeInTicks * 0.075F);

        float xRot;
        float zRot;
        if (pEntity.isGenocide()) {
            xRot = 0.6f + (0.05f * wing * (float) Math.PI);
            zRot = -0.96f - (0.1f * wing * (float) Math.PI);
        } else {
            xRot = 0.2f + (0.01f * wing * (float) Math.PI);
            zRot = -0.36f - (0.02f * wing * (float) Math.PI);
        }


        pEntity.wingModelXRot += (xRot - pEntity.wingModelXRot) * 0.1F;
        pEntity.wingModelZRot += (zRot - pEntity.wingModelZRot) * 0.1F;
        this.leftWing.xRot = pEntity.wingModelXRot;
        this.leftWing.zRot = pEntity.wingModelZRot;

        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }

    private void setupHeadRotation(FumetsuEntity entity, ModelPart part, int head, float partialTick) {
        part.yRot = (float) Math.toRadians(Mth.rotLerp(partialTick, entity.getOldHeadYRot(head), entity.getHeadYRot(head)) - entity.yBodyRot);
        part.xRot = (float) Math.toRadians(Mth.rotLerp(partialTick, entity.getOldHeadXRot(head), entity.getHeadXRot(head)));
    }
}