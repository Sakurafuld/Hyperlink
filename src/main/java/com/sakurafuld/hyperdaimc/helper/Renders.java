package com.sakurafuld.hyperdaimc.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sakurafuld.hyperdaimc.HyperSetup;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

@OnlyIn(Dist.CLIENT)
public class Renders {
    private Renders() {
    }

    private static final RandomSource RANDOM = RandomSource.create();
    private static final Direction[] QUAD_FACES = Arrays.copyOf(Direction.values(), Direction.values().length + 1);

    public static Supplier<BakedModel> importSpecialModel(String path) {
        ResourceLocation identifier = identifier("special/" + path);
        HyperSetup.specialModels.add(identifier);
        return () -> Minecraft.getInstance().getModelManager().getModel(identifier);
    }

    public static void with(PoseStack poseStack, Runnable runnable) {
        poseStack.pushPose();
        runnable.run();
        poseStack.popPose();
    }

    public static MultiBufferSource.BufferSource bufferSource() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public static VertexConsumer getBuffer(RenderType type) {
        return bufferSource().getBuffer(type);
    }

    public static void endBatch(RenderType type) {
        bufferSource().endBatch(type);
    }

    public static void cube(Matrix4f matrix, VertexConsumer builder, float startX, float startY, float startZ, float endX, float endY, float endZ, int color, Predicate<Direction> predicate) {
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        // Down.
        if (predicate.test(Direction.DOWN)) {
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).normal(0, -1, 0).endVertex();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).normal(0, -1, 0).endVertex();
        }

        // Up.
        if (predicate.test(Direction.UP)) {
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).normal(0, 1, 0).endVertex();
        }

        // North.
        if (predicate.test(Direction.NORTH)) {
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).normal(0, 0, -1).endVertex();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).normal(0, 0, -1).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).normal(0, 0, -1).endVertex();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).normal(0, 0, -1).endVertex();
        }

        // South.
        if (predicate.test(Direction.SOUTH)) {
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        }

        // West.
        if (predicate.test(Direction.WEST)) {
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).normal(-1, 0, 0).endVertex();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).normal(-1, 0, 0).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).normal(-1, 0, 0).endVertex();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).normal(-1, 0, 0).endVertex();
        }

        // East.
        if (predicate.test(Direction.EAST)) {
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).normal(1, 0, 0).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).normal(1, 0, 0).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).normal(1, 0, 0).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).normal(1, 0, 0).endVertex();
        }
    }

    public static void cubeScaled(Matrix4f matrix, VertexConsumer builder, float scale, int color, Predicate<Direction> predicate) {
        float startX = 0 + (1 - scale) / 2;
        float startY = 0 + (1 - scale) / 2;
        float startZ = 0 + (1 - scale) / 2;
        float endX = 1 - (1 - scale) / 2;
        float endY = 1 - (1 - scale) / 2;
        float endZ = 1 - (1 - scale) / 2;

        cube(matrix, builder, startX, startY, startZ, endX, endY, endZ, color, predicate);
    }

    public static void cubeBox(Matrix4f matrix, VertexConsumer builder, AABB aabb, int color, Predicate<Direction> predicate) {
        cube(matrix, builder, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ, color, predicate);
    }

    public static void hollowTriangle(Matrix4f matrix, VertexConsumer builder, float radius, float width, int color) {
        float angle = (float) Math.toRadians(360d / 3d);

        float x0 = Mth.cos(angle);
        float y0 = Mth.sin(angle);
        float x1 = Mth.cos(angle * 2);
        float y1 = Mth.sin(angle * 2);
        float x2 = Mth.cos(angle * 3);
        float y2 = Mth.sin(angle * 3);

        float outerX0 = x0 * radius;
        float outerY0 = y0 * radius;
        float outerX1 = x1 * radius;
        float outerY1 = y1 * radius;
        float outerX2 = x2 * radius;
        float outerY2 = y2 * radius;

        float innerX0 = x0 * (radius - width);
        float innerY0 = y0 * (radius - width);
        float innerX1 = x1 * (radius - width);
        float innerY1 = y1 * (radius - width);
        float innerX2 = x2 * (radius - width);
        float innerY2 = y2 * (radius - width);

        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.vertex(matrix, outerX0, outerY0, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, outerX1, outerY1, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, innerX1, innerY1, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, innerX0, innerY0, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();

        builder.vertex(matrix, outerX1, outerY1, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, outerX2, outerY2, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, innerX2, innerY2, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, innerX1, innerY1, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();

        builder.vertex(matrix, outerX2, outerY2, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, outerX0, outerY0, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, innerX0, innerY0, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
        builder.vertex(matrix, innerX2, innerY2, 0).color(red, green, blue, alpha).normal(0, 0, 1).endVertex();
    }

    public static void model(BakedModel model, ItemStack stack, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, Function<BakedQuad, Integer> colorizer) {
        for (Direction face : QUAD_FACES) {
            RANDOM.setSeed(42);
            for (BakedModel pass : model.getRenderPasses(stack, true)) {
                for (RenderType type : pass.getRenderTypes(stack, true)) {
                    for (BakedQuad quad : pass.getQuads(null, face, RANDOM, ModelData.EMPTY, type)) {
                        int color = colorizer.apply(quad);

                        float alpha = ((color >> 24) & 0xFF) / 255f;
                        float red = ((color >> 16) & 0xFF) / 255f;
                        float green = ((color >> 8) & 0xFF) / 255f;
                        float blue = (color & 0xFF) / 255f;

                        consumer.putBulkData(poseStack.last(), quad, red, green, blue, alpha, light, overlay, true);
                        require(EMBEDDIUM).run(() -> Embeddium.INSTANCE.activateSprite(quad));
                    }
                }
            }

        }
    }

    public static void model(BakedModel model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, Function<BakedQuad, Integer> colorizer) {
        model(model, ItemStack.EMPTY, poseStack, consumer, light, overlay, colorizer);
    }

    public static void model(BakedModel model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        model(model, poseStack, consumer, light, overlay, quad -> 0xFFFFFFFF);
    }

    public static void fluidStack(PoseStack poseStack, FluidStack stack, float x, float y) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(stack.getRawFluid());

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(stack));

        int color = extensions.getTintColor(stack);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        builder.vertex(matrix, x + 0, y + 0, 0).color(red, green, blue, 255).uv(u0, v0).endVertex();
        builder.vertex(matrix, x + 0, y + 16, 0).color(red, green, blue, 255).uv(u0, v1).endVertex();
        builder.vertex(matrix, x + 16, y + 16, 0).color(red, green, blue, 255).uv(u1, v1).endVertex();
        builder.vertex(matrix, x + 16, y + 0, 0).color(red, green, blue, 255).uv(u1, v0).endVertex();
        tesselator.end();
    }

    public static void slotScaledString(PoseStack poseStack, String text, double x, double y) {
        Font font = Minecraft.getInstance().font;

        int width = font.width(text);
        float scale = 16f / Math.max(16f, width);

        double dx = (x + 19 - 2) * (1 / scale) - font.width(text);
        double dy = (y + 6 + 3 + 8) * (1 / scale) - font.lineHeight;

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        Renders.with(poseStack, () -> {
            poseStack.scale(scale, scale, 1);
            poseStack.translate(dx, dy, 0);
            font.drawInBatch(text, 0, 0, 0xFFFFFF, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            bufferSource.endBatch();
        });
    }

    public static void fluidAmount(PoseStack poseStack, long amount, double x, double y) {

        slotScaledString(poseStack, Writes.fluidAmount(amount), x, y);
    }

    public enum Embeddium {
        INSTANCE;

        public void activateSprite(BakedQuad quad) {
            if (quad instanceof ModelQuadView view) {
                SpriteUtil.markSpriteActive(view.getSprite());
            }
        }
    }

    public static class Type extends RenderType {

        public Type(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        public static final RenderType HIGHLIGHT = create(HYPERDAIMC + ":highlight",
                DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 256, true, true,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

        public static final RenderType LIGHTNING_NO_CULL = create(HYPERDAIMC + ":lightning_no_cull",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
                        .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY)
                        .setOutputState(WEATHER_TARGET)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));


        private static final Function<ResourceLocation, RenderType> ADDITIVE_ENTITY_TRANSLUCENT = Util.memoize(texture ->
                create(HYPERDAIMC + ":additive_entity_translucent",
                        DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, CompositeState.builder()
                                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                                .setCullState(NO_CULL)
                                .setLightmapState(LIGHTMAP)
                                .setOverlayState(OVERLAY)
                                .createCompositeState(true)));

        public static RenderType additiveEntityTranslucent(ResourceLocation texture) {
            return ADDITIVE_ENTITY_TRANSLUCENT.apply(texture);
        }
    }
}
