package com.sakurafuld.hyperdaimc.content.crafting.chemical;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ChemicalHandler {
    public static final String TAG_MUTATION = HYPERDAIMC + ":mutation";
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void mutation(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Zombie zombie) {
            CompoundTag tag = zombie.getPersistentData();
            if (tag.contains(TAG_MUTATION, Tag.TAG_INT)) {
                int mutation = tag.getInt(TAG_MUTATION);
                if (mutation >= 0) {
                    tag.putInt(TAG_MUTATION, mutation + 1);
                    double delta = mutation / 80d;
                    if (zombie.getRandom().nextFloat() < Calculates.curve(delta, 0.1, 0.4, 0.9)) {
                        zombie.setXRot(zombie.getRandom().nextFloat() * 360);
                        zombie.setYRot(zombie.getRandom().nextFloat() * 360);
                        zombie.setYBodyRot(zombie.getRandom().nextFloat() * 360);
                    }
                    require(LogicalSide.SERVER).run(() -> {
                        if (mutation > 80 && !MutekiHandler.muteki(zombie)) {
                            Player player = zombie.level().getNearestPlayer(zombie, 32);
                            double yRot;
                            if (player != null) {
                                Vec3 vec = player.position().subtract(zombie.position());
                                yRot = Math.toDegrees(Mth.atan2(vec.x(), vec.z())) + 90;
                            } else {
                                yRot = zombie.yBodyRot;
                            }
                            Direction face = Direction.fromYRot(yRot);
                            BlockPos pos = zombie.blockPosition().above();
                            BlockState state = HyperBlocks.SOUL.get().defaultBlockState();

                            List<BlockPos> list = Lists.newArrayList(pos.below(), pos);
                            if (face.getAxis() == Direction.Axis.X) {
                                list.add(pos.west());
                                list.add(pos.east());
                            } else {
                                list.add(pos.north());
                                list.add(pos.south());
                            }

                            list.forEach(at -> {
                                if (zombie.level().getBlockState(at).canBeReplaced()) {
                                    zombie.level().setBlockAndUpdate(at, state);
                                } else {
                                    Block.popResource(zombie.level(), at, state.getBlock().asItem().getDefaultInstance());
                                }
                                zombie.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, at, Block.getId(state));
                                zombie.playSound(SoundEvents.ZOMBIE_DEATH, 1, 0.5f);
                            });

                            zombie.discard();
                        }
                    });
                } else {
                    tag.remove(TAG_MUTATION);
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLivingEvent.Pre<? extends Zombie, ? extends HumanoidModel<? extends LivingEntity>> event) {
        if (event.getEntity() instanceof Zombie zombie && zombie.getPersistentData().contains(TAG_MUTATION)) {
            int mutation = zombie.getPersistentData().getInt(TAG_MUTATION);
            if (mutation >= 0) {
                float delta = mutation / 80f;
                delta = (float) Calculates.curve(delta, 0.1, 0.3, 1);
                event.getPoseStack().translate(RANDOM.nextDouble(-0.1, 0.1) * delta, RANDOM.nextDouble(-0.1, 0.1) * delta, RANDOM.nextDouble(-0.1, 0.1) * delta);
                delta = (float) Calculates.curve(delta, 0, 0.05, 0.15);
                event.getPoseStack().scale(RANDOM.nextFloat(1 - delta, 1 + delta), RANDOM.nextFloat(1 - delta, 1 + delta), RANDOM.nextFloat(1 - delta, 1 + delta));
            }
        }
    }
}
