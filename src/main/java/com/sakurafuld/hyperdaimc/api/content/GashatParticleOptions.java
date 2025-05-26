package com.sakurafuld.hyperdaimc.api.content;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sakurafuld.hyperdaimc.content.HyperParticles;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class GashatParticleOptions implements ParticleOptions {
    public static final Codec<GashatParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            Vector3f.CODEC.fieldOf("color").forGetter(self -> self.color),
                            Codec.FLOAT.fieldOf("radius").forGetter(self -> self.radius),
                            Codec.FLOAT.fieldOf("width").forGetter(self -> self.width),
                            Codec.FLOAT.fieldOf("speed").forGetter(self -> self.speed))
                    .apply(instance, GashatParticleOptions::new));
    public static final ParticleOptions.Deserializer<GashatParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public GashatParticleOptions fromCommand(ParticleType<GashatParticleOptions> pParticleType, StringReader pReader) throws CommandSyntaxException {
            Vector3f color = DustParticleOptionsBase.readVector3f(pReader);
            pReader.expect(' ');
            float radius = pReader.readFloat();
            pReader.expect(' ');
            float width = pReader.readFloat();
            pReader.expect(' ');
            float speed = pReader.readFloat();
            return new GashatParticleOptions(color, radius, width, speed);
        }

        @Override
        public GashatParticleOptions fromNetwork(ParticleType<GashatParticleOptions> pParticleType, FriendlyByteBuf pBuffer) {
            return new GashatParticleOptions(DustParticleOptionsBase.readVector3f(pBuffer), pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat());
        }
    };


    public final Vector3f color;
    public final float radius;
    public final float width;
    public final float speed;

    public GashatParticleOptions(Vector3f color, float radius, float width, float speed) {
        this.color = color;
        this.radius = radius;
        this.width = width;
        this.speed = speed;
    }

    @Override
    public ParticleType<?> getType() {
        return HyperParticles.GASHAT.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeFloat(this.color.x());
        pBuffer.writeFloat(this.color.y());
        pBuffer.writeFloat(this.color.z());
        pBuffer.writeFloat(this.radius);
        pBuffer.writeFloat(this.width);
        pBuffer.writeFloat(this.speed);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.radius, this.width, this.speed);
    }
}
