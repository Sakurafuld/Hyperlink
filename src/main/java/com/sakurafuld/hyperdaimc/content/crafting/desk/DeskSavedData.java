package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.api.content.GashatParticleOptions;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.desk.ClientboundDeskSyncSave;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class DeskSavedData extends SavedData {
    private static final Object2ObjectOpenHashMap<ResourceKey<Level>, DeskSavedData> client = new Object2ObjectOpenHashMap<>();

    private final Set<Entry> entries = Sets.newHashSet();

    private DeskSavedData() {
    }

    private DeskSavedData(CompoundTag tag) {
        this.load(tag);
    }

    public static DeskSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(DeskSavedData::new, DeskSavedData::new, HYPERDAIMC + "_desk");
        } else {
            return client.computeIfAbsent(level.dimension(), dimension -> new DeskSavedData());
        }
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public Set<Entry> getEntries() {
        return this.entries;
    }

    public Entry add(BlockPos pos, List<ItemStack> ingredients, ItemStack result) {
        Entry entry = new Entry(pos, ingredients.stream()
                .map(ingredient -> ItemHandlerHelper.copyStackWithSize(ingredient, 1))
                .toList(), result.copy());

        this.entries.add(entry);
        return entry;
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag entries = new ListTag();
        for (Entry entry : this.entries) {
            entries.add(entry.save());
        }
        pCompoundTag.put("Entries", entries);
        return pCompoundTag;
    }

    public void load(CompoundTag tag) {
        this.entries.clear();
        tag.getList("Entries", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .map(Entry::load)
                .forEach(this.entries::add);
    }

    public void sync2Client(ServerPlayer player) {
        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundDeskSyncSave(this.save(new CompoundTag())));
    }

    public void sync2Client(Supplier<ResourceKey<Level>> dimension) {
        HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(dimension), new ClientboundDeskSyncSave(this.save(new CompoundTag())));
    }

    public static class Entry {
        private static final Random RANDOM = new Random();
        public final BlockPos pos;
        public final List<One> ingredients;
        public final ItemStack result;
        private int ticks = 0;
        private boolean renderOnes = true;
        public long seed;
        public float itemSize = 0;
        public float oldItemSize = 0;
        public Vector3f rot = new Vector3f();
        public Vector3f oldRot = new Vector3f();
        public float fxAlpha = 1;
        public float fxSize = 0;
        public float oldFXSize = 0;

        private Entry(BlockPos pos, List<ItemStack> ingredients, ItemStack result) {
            this(pos, ImmutableList.copyOf(Util.make(Lists.<One>newArrayList(), list -> {
                IntList ints = new IntArrayList();

                for (int index = 0; index < ingredients.size(); index++) {
                    ints.add(index);
                }
                IntLists.shuffle(ints, RANDOM);
                double lastAngle = RANDOM.nextDouble(360);
                for (int index = 0; index < ingredients.size(); index++) {
                    list.add(new One(ingredients.get(index), index / (float) ingredients.size(), ints.getInt(index), RANDOM.nextDouble(2, 4), RANDOM.nextDouble(4), lastAngle));
                }
            })), result);
        }

        private Entry(BlockPos pos, ImmutableList<One> ingredients, ItemStack result) {
            this.pos = pos;
            this.ingredients = ingredients;
            this.result = result;
            this.seed = pos.asLong();
        }

        public boolean tick(Level level) {
            int max = 43 + this.ingredients.size() + this.ingredients.size() + this.ingredients.size();
            if (++this.ticks > max) {
                this.renderOnes = false;
                if (this.ticks == max + 1) {
                    Vec3 center = Vec3.atCenterOf(this.pos);
                    for (int count = 0; count < 128; ++count) {
                        double dx = level.getRandom().nextFloat() * 2 - 1;
                        double dy = level.getRandom().nextFloat() * 2 - 1;
                        double dz = level.getRandom().nextFloat() * 2 - 1;
                        double x = center.x() + (dx / 4);
                        double y = center.y() + (0.5D + dy / 4);
                        double z = center.z() + (dz / 4);
                        level.addParticle(new GashatParticleOptions(new Vector3f(level.getRandom().nextFloat(), level.getRandom().nextFloat(), level.getRandom().nextFloat()), 0.25f, 0.1f, 64), false, x, y, z, dx, dy, dz);
                    }
                    level.playSound(null, this.pos, HyperSounds.DESK_MINECRAFT_FINISH.get(), SoundSource.MASTER, 0.75f, 0.5f);
                    level.playSound(null, this.pos, HyperSounds.DESK_MINECRAFT_FINISH.get(), SoundSource.MASTER, 0.75f, 1);
                    level.playSound(null, this.pos, HyperSounds.DESK_MINECRAFT_FINISH.get(), SoundSource.MASTER, 1.25f, 2.5f);

                    ItemEntity result = new ItemEntity(level, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.result, level.getRandom().nextDouble() * 0.2 - 0.1, 0.6, level.getRandom().nextDouble() * 0.2 - 0.1);
                    result.setNoPickUpDelay();
                    result.setUnlimitedLifetime();
                    level.addFreshEntity(result);
                }

                if (this.ticks <= max + 20) {
                    this.oldFXSize = this.fxSize;

                    double delta = (this.ticks - max) / 20d;

                    this.fxAlpha = (float) Calculates.curve(delta, 1, 0.75, 0);
                    this.fxSize = (float) Calculates.curve(delta, 0, 3, 12);
                    return true;
                } else {
                    return false;
                }
            } else {
                if (this.ticks == 1) {
                    level.playSound(null, this.pos, HyperSounds.DESK_MINECRAFT_START.get(), SoundSource.MASTER, 1, 1.5f);
                }

                this.oldItemSize = this.itemSize;
                this.oldRot = new Vector3f(this.rot);

                double delta = Math.min(1, this.ticks / 20d);

                RANDOM.setSeed(this.seed);
                float xRot = (float) Math.toRadians(this.ticks * RANDOM.nextFloat(-511, 512));
                float yRot = (float) Math.toRadians(this.ticks * RANDOM.nextFloat(-511, 512));
                float zRot = (float) Math.toRadians(this.ticks * RANDOM.nextFloat(-511, 512));


                this.itemSize = (float) Calculates.curve(delta, 0, 0.2, 1);
                this.rot.set(xRot, yRot, zRot);

                this.ingredients.forEach(one -> one.tick(level, this));
                return true;
            }
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();

            tag.putLong("Pos", this.pos.asLong());

            ListTag list = new ListTag();
            for (One ingredient : this.ingredients) {
                list.add(ingredient.save());
            }

            tag.put("Ingredients", list);
            tag.put("Result", this.result.save(new CompoundTag()));
            tag.putInt("Ticks", this.ticks);

            tag.putBoolean("RenderOnes", this.renderOnes);
            tag.putLong("Seed", this.seed);
            tag.putFloat("ItemSize", this.itemSize);
            tag.putFloat("OldItemSize", this.oldItemSize);

            tag.putFloat("RotX", this.rot.x());
            tag.putFloat("RotY", this.rot.y());
            tag.putFloat("RotZ", this.rot.z());
            tag.putFloat("OldRotX", this.oldRot.x());
            tag.putFloat("OldRotY", this.oldRot.y());
            tag.putFloat("OldRotZ", this.oldRot.z());

            tag.putFloat("FXAlpha", this.fxAlpha);
            tag.putFloat("FXSize", this.fxSize);
            tag.putFloat("OldFXSize", this.oldFXSize);

            return tag;
        }

        public static Entry load(CompoundTag tag) {
            BlockPos pos = BlockPos.of(tag.getLong("Pos"));

            List<One> ingredients = tag.getList("Ingredients", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(One::load)
                    .toList();

            ItemStack result = ItemStack.of(tag.getCompound("Result"));
            Entry entry = new Entry(pos, ImmutableList.copyOf(ingredients), result);

            entry.ticks = tag.getInt("Ticks");
            entry.renderOnes = tag.getBoolean("RenderOnes");
            entry.seed = tag.getLong("Seed");
            entry.itemSize = tag.getFloat("ItemSize");
            entry.oldItemSize = tag.getFloat("OldItemSize");

            entry.rot = new Vector3f(tag.getFloat("RotX"), tag.getFloat("RotY"), tag.getFloat("RotZ"));
            entry.oldRot = new Vector3f(tag.getFloat("OldRotX"), tag.getFloat("OldRotY"), tag.getFloat("OldRotZ"));

            entry.fxAlpha = tag.getFloat("FXAlpha");
            entry.fxSize = tag.getFloat("FXSize");
            entry.oldFXSize = tag.getFloat("OldFXSize");

            return entry;
        }

        public boolean shouldRenderOnes() {
            return this.renderOnes;
        }
    }

    public static class One {
        private final Random random = new Random();
        public final ItemStack stack;
        public final float percent;
        public final int index;
        public final double reach;
        public final double y;
        public final double angle;
        public int ticks = 0;
        public Vec3 position = Vec3.ZERO;
        public Vec3 oldPosition = Vec3.ZERO;
        public Vector3f rot = new Vector3f();
        public Vector3f oldRot = new Vector3f();
        public float radius = 0;
        public int color;

        private One(ItemStack stack, float percent, int index, double reach, double y, double angle) {
            this.stack = stack;
            this.percent = percent;
            this.index = index + 1;
            this.reach = reach;
            this.y = y;
            this.angle = angle;
            this.color = (0xFF << 24) | (this.random.nextBoolean() ? 0xFF0101 : 0x01FFFF);
        }

        public void tick(Level level, Entry entry) {
            int max = 40 + entry.ingredients.size() + entry.ingredients.size() + this.index;
            // こっちじゃなくて.
//            this.oldPosition = this.position;
//            this.oldRot = this.rot.copy();
            if (++this.ticks <= max) {
                // こっちに置いた方がpartialTickのアレですごいいい感じ(超偶然見つけた).
                this.oldPosition = this.position;
                this.oldRot = new Vector3f(this.rot);

                double first = 20 + this.index;
                double second = first + entry.ingredients.size() + 5;
                double third = second + this.index;
                double angle = this.percent * 360d;
                if (this.ticks <= first) {
                    double delta = this.ticks / first;
                    delta = Calculates.curve(delta, 0, 0.2, 1);
                    delta = Calculates.curve(delta, 0, 0.1, 1);
                    double x = Math.cos(Math.toRadians(angle)) * Mth.lerp(delta, 0, this.reach);
                    double y = Mth.lerp(delta, 0, this.y);
                    double z = Math.sin(Math.toRadians(angle)) * Mth.lerp(delta, 0, this.reach);

                    this.random.setSeed(this.index * 8192L);
                    float xRot = (float) Math.toRadians(this.ticks * this.random.nextFloat(-511, 512));
                    float yRot = (float) Math.toRadians(this.ticks * this.random.nextFloat(-511, 512));
                    float zRot = (float) Math.toRadians(this.ticks * this.random.nextFloat(-511, 512));

                    this.position = new Vec3(x, y, z);
                    this.rot.set(xRot, yRot, zRot);

                    if (this.ticks == first) {
                        Vec3 center = Vec3.atCenterOf(entry.pos);
                        Vec3 at = center.add(x, y, z);
                        delta = (float) this.index / (float) entry.ingredients.size();
                        float pitch = (float) Math.pow(2, ((delta * 24) - 12) / 12d);
                        level.playSound(null, at.x(), at.y(), at.z(), HyperSounds.DESK_POP.get(), SoundSource.MASTER, 0.5f, pitch + this.random.nextFloat() * 0.2f);
                        Vector3f color = new Vector3f((this.color >> 16 & 0xFF) / 255f, (this.color >> 8 & 0xFF) / 255f, (this.color & 0xFF) / 255f);
                        for (int count = 0; count < 16; count++) {
                            level.addParticle(new GashatParticleOptions(color, 0.15f, 0.05f, 16), at.x(), at.y(), at.z(), (this.random.nextDouble() - 0.5) * 2, (this.random.nextDouble() - 0.5) * 2, (this.random.nextDouble() - 0.5) * 2);
                        }
                    }
                } else if (this.ticks <= second) {
                    double delta = Math.min(1, (this.ticks - first) / 5d);
                    double blur = Calculates.curve(delta, 0.5, 0.4, 0);
                    double x = Math.cos(Math.toRadians(angle)) * (this.reach + blur * (this.random.nextDouble() - 0.5));
                    double y = this.y + blur * (this.random.nextDouble() - 0.5);
                    double z = Math.sin(Math.toRadians(angle)) * (this.reach + blur * (this.random.nextDouble() - 0.5));

                    this.position = new Vec3(x, y, z);
                    this.radius = 0.7f;
                } else if (this.ticks > third) {

                    double delta = Math.min(1, (this.ticks - third) / (max - third));
                    delta = Calculates.curve(delta, 0, 0.45, 0.55, 1);
                    delta = Calculates.curve(delta, 0, 0.45, 1);
                    Vec3 p1 = new Vec3(Math.cos(Math.toRadians(angle)) * this.reach, this.y, Math.sin(Math.toRadians(angle)) * this.reach);
                    Vec3 p2 = new Vec3(Math.cos(Math.toRadians(this.angle)) * 16, 6, Math.sin(Math.toRadians(this.angle)) * 16);
                    Vec3 p3 = new Vec3(Math.cos(Math.toRadians(this.angle + 120)) * 22, 14, Math.sin(Math.toRadians(this.angle + 120)) * 22);
                    Vec3 p4 = new Vec3(Math.cos(Math.toRadians(this.angle + 240)) * 22, 26, Math.sin(Math.toRadians(this.angle + 240)) * 22);

                    double x = Calculates.curve(delta, p1.x(), p2.x(), p3.x(), p4.x(), 0);
                    double y = Calculates.curve(delta, p1.y(), p2.y(), p3.y(), p4.y(), 0);
                    double z = Calculates.curve(delta, p1.z(), p2.z(), p3.z(), p4.z(), 0);

                    this.position = new Vec3(x, y, z);

                    Vec3 at = Vec3.atCenterOf(entry.pos).add(x, y, z);
                    if (this.ticks - 1 == third) {
                        level.playSound(null, at.x(), at.y(), at.z(), HyperSounds.DESK_MINECRAFT_FLAP.get(), SoundSource.MASTER, 0.5f, 1 + this.random.nextFloat() * 0.2f);
                    } else if (this.ticks == max) {
                        entry.seed = this.random.nextLong(Long.MAX_VALUE);
                        delta = (float) this.index / (float) entry.ingredients.size();
                        float pitch = (float) Math.pow(2, ((delta * 24) - 12) / 12d);
                        level.playSound(null, at.x(), at.y(), at.z(), HyperSounds.DESK_RESULT.get(), SoundSource.MASTER, 0.5f, pitch + (this.random.nextFloat() * 0.2f - 0.1f));
                        Vector3f color = new Vector3f((this.color >> 16 & 0xFF) / 255f, (this.color >> 8 & 0xFF) / 255f, (this.color & 0xFF) / 255f);
                        for (int count = 0; count < 8; count++) {
                            level.addParticle(new GashatParticleOptions(color, 0.3f, 0.1f, 32), at.x(), at.y(), at.z(), (this.random.nextDouble() - 0.5) * 2, (this.random.nextDouble() - 0.5) * 2, (this.random.nextDouble() - 0.5) * 2);
                        }
                    }
                }
            }
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();

            tag.put("Item", this.stack.save(new CompoundTag()));
            tag.putFloat("Percent", this.percent);
            tag.putInt("Index", this.index);
            tag.putDouble("Reach", this.reach);
            tag.putDouble("Y", this.y);
            tag.putDouble("Angle", this.angle);
            tag.putInt("Ticks", this.ticks);

            tag.putDouble("PosX", this.position.x());
            tag.putDouble("PosY", this.position.y());
            tag.putDouble("PosZ", this.position.z());
            tag.putDouble("OldPosX", this.oldPosition.x());
            tag.putDouble("OldPosY", this.oldPosition.y());
            tag.putDouble("OldPosZ", this.oldPosition.z());

            tag.putFloat("RotX", this.rot.x());
            tag.putFloat("RotY", this.rot.y());
            tag.putFloat("RotZ", this.rot.z());
            tag.putFloat("OldRotX", this.oldRot.x());
            tag.putFloat("OldRotY", this.oldRot.y());
            tag.putFloat("OldRotZ", this.oldRot.z());

            tag.putFloat("Radius", this.radius);
            tag.putInt("Color", this.color);

            return tag;
        }

        public static One load(CompoundTag tag) {
            ItemStack stack = ItemStack.of(tag.getCompound("Item"));
            float percent = tag.getFloat("Percent");
            int index = tag.getInt("Index");
            double reach = tag.getDouble("Reach");
            double y = tag.getDouble("Y");
            double lastAngle = tag.getDouble("Angle");

            One one = new One(stack, percent, index, reach, y, lastAngle);

            one.ticks = tag.getInt("Ticks");
            one.position = new Vec3(tag.getDouble("PosX"), tag.getDouble("PosY"), tag.getDouble("PosZ"));
            one.oldPosition = new Vec3(tag.getDouble("OldPosX"), tag.getDouble("OldPosY"), tag.getDouble("OldPosZ"));
            one.rot = new Vector3f(tag.getFloat("RotX"), tag.getFloat("RotY"), tag.getFloat("RotZ"));
            one.oldRot = new Vector3f(tag.getFloat("OldRotX"), tag.getFloat("OldRotY"), tag.getFloat("OldRotZ"));
            one.radius = tag.getFloat("Radius");
            one.color = tag.getInt("Color");

            return one;
        }
    }
}
