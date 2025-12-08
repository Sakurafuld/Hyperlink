package com.sakurafuld.hyperdaimc.content.crafting.skull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class FumetsuSkullBlockEntityRenderer implements BlockEntityRenderer<FumetsuSkullBlockEntity> {
    public static final ResourceLocation TEXTURE = identifier("textures/block/fumetsu_skull.png");

    private final Object2ObjectOpenHashMap<Block, SkullModel> models;
    private final SkullModel center;

    public FumetsuSkullBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
        this.models = new Object2ObjectOpenHashMap<>();
        SkullModel model;
        model = new SkullModel(create(0, 0).bakeRoot());
        this.center = model;
        this.models.put(HyperBlocks.FUMETSU_SKULL.get(), model);
        this.models.put(HyperBlocks.FUMETSU_WALL_SKULL.get(), model);
        model = new SkullModel(create(0, 16).bakeRoot());
        this.models.put(HyperBlocks.FUMETSU_RIGHT.get(), model);
        this.models.put(HyperBlocks.FUMETSU_WALL_RIGHT.get(), model);
        model = new SkullModel(create(32, 0).bakeRoot());
        this.models.put(HyperBlocks.FUMETSU_LEFT.get(), model);
        this.models.put(HyperBlocks.FUMETSU_WALL_LEFT.get(), model);
    }

    public static LayerDefinition create(int x, int y) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(x, y).addBox(-4, -8, -4, 8, 8, 8), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(FumetsuSkullBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        boolean wall = state.getBlock() instanceof WallSkullBlock;
        Direction direction = wall ? state.getValue(WallSkullBlock.FACING) : null;
        float yRot = 22.5f * (wall ? (2f + direction.get2DDataValue()) * 4f : state.getValue(SkullBlock.ROTATION));

        SkullBlockRenderer.renderSkull(direction, yRot, 0, pPoseStack, pBufferSource, pPackedLight, this.models.getOrDefault(pBlockEntity.getBlockState().getBlock(), this.center), RenderType.entityCutoutNoCullZOffset(TEXTURE));
    }
}
