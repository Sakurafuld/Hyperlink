package com.sakurafuld.hyperdaimc.addon.jei.information;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.library.gui.ingredients.CycleTimer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Stream;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class SoulInformation extends HyperInformation {
    private static final RandomSource RANDOM = RandomSource.create();
    private final CycleTimer timer = CycleTimer.create(3);
    private final List<Optional<Zombie>> zombies;
    private final VillagerProfession[] professions;
    private final Set<Particle> particles = new ObjectOpenHashSet<>();
    private final IDrawable chemicalMax;
    private double lastDelta;
    private Zombie lastZombie;

    public SoulInformation(IGuiHelper helper) {
        super(helper, HyperItems.CHEMICAL_MAX.get(), Items.ZOMBIE_SPAWN_EGG, HyperBlocks.SOUL.get().asItem());

        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        Zombie zombie = new Zombie(level);
        ZombieVillager villager = new ZombieVillager(EntityType.ZOMBIE_VILLAGER, level);
        Husk husk = new Husk(EntityType.HUSK, level);
        Drowned drowned = new Drowned(EntityType.DROWNED, level);
        ZombifiedPiglin piglin = new ZombifiedPiglin(EntityType.ZOMBIFIED_PIGLIN, level);
        this.zombies = Stream.of(zombie, villager, husk, drowned, piglin).map(Optional::of).toList();
        Collection<VillagerProfession> professions = ForgeRegistries.VILLAGER_PROFESSIONS.getValues();
        this.professions = professions.isEmpty() ? null : professions.toArray(new VillagerProfession[0]);
        this.chemicalMax = helper.createDrawableItemLike(HyperItems.CHEMICAL_MAX.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(GuiGraphics graphics, double mouseX, double mouseY) {
        PoseStack poseStack = graphics.pose();

        double delta = Math.min(1, Util.getMillis() * 2 % 3000d / 1000d);
        int x = Mth.lerpInt((float) delta, 0, 40);
        int y = (int) Math.round(Calculates.curve(delta, 35, 10, 30));
        if (delta < 0.95)
            this.chemicalMax.draw(graphics, x, y);
        else if (this.lastDelta <= 0.95) {
            for (int i = 0; i < 16; i++)
                this.particles.add(new Particle(8 + x + (RANDOM.nextFloat() * 2 - 1) * 4, 8 + y + (RANDOM.nextFloat() * 2 - 1) * 4));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SPLASH_POTION_BREAK, RANDOM.nextFloat() * 0.1f + 0.9f));
        }
        this.lastDelta = delta;

        if (!this.particles.isEmpty())
            this.particles.removeIf(particle -> !particle.render(graphics));

        this.timer.getCycled(this.zombies).ifPresent(zombie -> {
            if (this.lastZombie != zombie && zombie instanceof VillagerDataHolder villager) {
                VillagerProfession profession = Util.getRandom(this.professions, zombie.getRandom());
                villager.setVillagerData(villager.getVillagerData().setProfession(profession));
            }
            Renders.with(poseStack, () -> {
                int offsetX = 50;
                int offsetY = 60;
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, offsetX, offsetY, 15, offsetX - (float) mouseX, offsetY - (float) mouseY, zombie);
            });
            this.lastZombie = zombie;
        });

        this.drawArrow(graphics, 68, 40);

        Renders.with(poseStack, () -> {
            poseStack.translate(110, 40, 100);
            blockTransform(poseStack);
            this.drawSouls(graphics);
        });
    }

    private static class Particle {
        private static final TextureAtlasSprite SPRITE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(identifier("item/chemical_max0"));
        private static final long DURATION = 500;

        private final long made = Util.getMillis();
        private final float uo;
        private final float vo;
        private Vec2 pos;
        private Vec2 move;
        private float lastTime = getTime();

        private Particle(float x, float y) {
            this.pos = new Vec2(x, y);
            this.move = new Vec2((RANDOM.nextFloat() * 2 - 1) * 4, RANDOM.nextFloat() * -6);
            this.uo = RANDOM.nextFloat() * 3;
            this.vo = RANDOM.nextFloat() * 3;
        }

        private boolean render(GuiGraphics graphics) {
            if (DURATION < Util.getMillis() - this.made) {
                return false;
            } else {
                float u0 = SPRITE.getU((this.uo + 1f) / 4f * 16f);
                float v0 = SPRITE.getV(this.vo / 4f * 16f);
                float u1 = SPRITE.getU(this.uo / 4f * 16f);
                float v1 = SPRITE.getV((this.vo + 1f) / 4f * 16f);

                PoseStack poseStack = graphics.pose();
                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

                Renders.with(poseStack, () -> {
                    poseStack.translate(this.pos.x, this.pos.y, 50);

                    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                    buffer.vertex(poseStack.last().pose(), -1, -1, 0).uv(u1, v1).color(0xFFFFFFFF).uv2(LightTexture.FULL_BRIGHT).endVertex();
                    buffer.vertex(poseStack.last().pose(), -1, 1, 0).uv(u1, v0).color(0xFFFFFFFF).uv2(LightTexture.FULL_BRIGHT).endVertex();
                    buffer.vertex(poseStack.last().pose(), 1, 1, 0).uv(u0, v0).color(0xFFFFFFFF).uv2(LightTexture.FULL_BRIGHT).endVertex();
                    buffer.vertex(poseStack.last().pose(), 1, -1, 0).uv(u0, v1).color(0xFFFFFFFF).uv2(LightTexture.FULL_BRIGHT).endVertex();

                    Tesselator.getInstance().end();
                });

                float time = getTime();
                float elapsed = time - this.lastTime;
                if (1 <= elapsed) {
                    float dx = this.move.x * 0.9f;
                    float dy = this.move.y + 0.9f;
                    this.move = new Vec2(this.move.x + (dx - this.move.x) * elapsed, this.move.y + (dy - this.move.y) * elapsed);
                    this.pos = this.pos.add(this.move);
                    this.lastTime = time;
                }

                return true;
            }
        }
    }
}
