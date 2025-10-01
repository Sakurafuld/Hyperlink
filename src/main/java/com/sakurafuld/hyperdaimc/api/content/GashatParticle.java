package com.sakurafuld.hyperdaimc.api.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

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
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        double x = Mth.lerp(pPartialTicks, this.xo, this.x) - pRenderInfo.getPosition().x();
        double y = Mth.lerp(pPartialTicks, this.yo, this.y) - pRenderInfo.getPosition().y();
        double z = Mth.lerp(pPartialTicks, this.zo, this.z) - pRenderInfo.getPosition().z();

        float ticks = (float) this.age / (float) this.lifetime;
        float ticksO = (float) (this.age - 1) / (float) this.lifetime;
        float size;
        if (ticks < 0.5) {
            size = Mth.lerp(pPartialTicks, ticksO / 0.5f, ticks / 0.5f);
        } else {
            size = Mth.lerp(pPartialTicks, (1 - ticksO) / 0.5f, (1 - ticks) / 0.5f);
        }

        PoseStack poseStack = new PoseStack();

        Renders.with(poseStack, () -> {
            poseStack.translate(x, y, z);
            poseStack.scale(size, size, size);

            poseStack.mulPose(Axis.YP.rotationDegrees(this.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(this.xRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(pPartialTicks, ((this.age - 1) % 360f) * this.options.speed(), (this.age % 360f) * this.options.speed())));

            Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), this.options.radius(), this.options.width(), this.color);

        });

        Renders.endBatch(Renders.Type.LIGHTNING_NO_CULL);
    }

    @Override
    public void tick() {
        super.tick();
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
