package com.sakurafuld.hyperdaimc.infrastructure.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.OCULUS;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.require;

public class GashatParticle extends Particle {
    private final GashatParticleOptions options;
    private final int color;
    private final float xRot;
    private final float yRot;

    public GashatParticle(GashatParticleOptions options, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.options = options;
        this.color = (0xFF << 24) | ((int) (options.color().x() * 0xFF) << 16) | ((int) (options.color().y() * 0xFF) << 8) | (int) (options.color().z() * 0xFF);
        this.xRot = pLevel.getRandom().nextInt(360);
        this.yRot = pLevel.getRandom().nextInt(360);
        this.setSize(options.radius(), options.radius());
        this.gravity = options.gravity();
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 camera = pRenderInfo.getPosition();
        double x = Mth.lerp(pPartialTicks, this.xo, this.x) - camera.x();
        double y = Mth.lerp(pPartialTicks, this.yo, this.y) - camera.y();
        double z = Mth.lerp(pPartialTicks, this.zo, this.z) - camera.z();

        float ticks = (float) this.age / (float) this.lifetime;
        float ticksO = (float) (this.age - 1) / (float) this.lifetime;
        float size;
        if (ticks < 0.5)
            size = Mth.lerp(pPartialTicks, ticksO / 0.5f, ticks / 0.5f);
        else
            size = Mth.lerp(pPartialTicks, (1 - ticksO) / 0.5f, (1 - ticks) / 0.5f);

        PoseStack poseStack = require(OCULUS) ? RenderSystem.getModelViewStack() : new PoseStack();
        Renders.with(poseStack, () -> {
            poseStack.translate(x, y, z);
            poseStack.scale(size, size, size);

            poseStack.mulPose(Axis.YP.rotationDegrees(this.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(this.xRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(pPartialTicks, ((this.age - 1) % 360f) * this.options.speed(), (this.age % 360f) * this.options.speed())));

            Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), this.options.radius(), this.options.width(), this.color);
        });

        Renders.endBatch(Renders.Type.LIGHTNING_NO_CULL);
//        if (oculus)
//            RenderSystem.applyModelViewMatrix();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<GashatParticleOptions> {

        @Nullable
        @Override
        public Particle createParticle(GashatParticleOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new GashatParticle(pType, pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
