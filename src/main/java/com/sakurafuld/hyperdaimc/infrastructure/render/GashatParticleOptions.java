package com.sakurafuld.hyperdaimc.infrastructure.render;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sakurafuld.hyperdaimc.content.HyperParticles;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public record GashatParticleOptions(Vector3f color, float radius, float width, float speed,
                                    float gravity) implements ParticleOptions {
    public GashatParticleOptions(Vector3f color, float radius, float width, float speed) {
        this(color, radius, width, speed, 0);
    }

    public static final Codec<GashatParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                            ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(self -> self.color),
                            Codec.FLOAT.fieldOf("radius").forGetter(self -> self.radius),
                            Codec.FLOAT.fieldOf("width").forGetter(self -> self.width),
                            Codec.FLOAT.fieldOf("speed").forGetter(self -> self.speed),
                            Codec.FLOAT.optionalFieldOf("gravity", 0f).forGetter(self -> self.gravity))
                    .apply(instance, GashatParticleOptions::new));
    public static final Deserializer<GashatParticleOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public GashatParticleOptions fromCommand(ParticleType<GashatParticleOptions> pParticleType, StringReader pReader) throws CommandSyntaxException {
            Vector3f color = DustParticleOptionsBase.readVector3f(pReader);
            pReader.expect(' ');
            float radius = pReader.readFloat();
            pReader.expect(' ');
            float width = pReader.readFloat();
            pReader.expect(' ');
            float speed = pReader.readFloat();
            pReader.expect(' ');
            float gravity = pReader.readFloat();
            return new GashatParticleOptions(color, radius, width, speed, gravity);
        }

        @Override
        public GashatParticleOptions fromNetwork(ParticleType<GashatParticleOptions> pParticleType, FriendlyByteBuf pBuffer) {
            return new GashatParticleOptions(DustParticleOptionsBase.readVector3f(pBuffer), pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat());
        }
    };

    public static GashatParticleOptions drop(Supplier<Float> random, float gravity) {
        Vector3f color = new Vector3f(random.get(), random.get(), random.get());
        float radius = 0.025f + random.get() * 0.15f;
        float width = radius * (0.75f - random.get() * 0.25f);
        float speed = random.get() * 48;
        return new GashatParticleOptions(color, radius, width, speed, gravity);
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
        pBuffer.writeFloat(this.gravity);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), this.color.x(), this.color.y(), this.color.z(), this.radius, this.width, this.speed);
    }
}
