package com.maxts.netherportalcontrol;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class PortalEventHandler {

    public static void register() {
        System.out.println("[NetherPortalControl] PortalEventHandler registering events...");

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity)) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() != Items.FLINT_AND_STEEL) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isOf(Blocks.OBSIDIAN)) {
                if (world.getRegistryKey() == World.NETHER) {
                    if (!NetherPortalControlMod.witherDefeated) {
                        player.sendMessage(Text.of("§cВы должны победить Иссушителя, чтобы активировать портал!"), false);

                        // Extinguish fire manually if fire appears above the obsidian
                        BlockPos firePos = pos.up();
                        if (world.getBlockState(firePos).isOf(Blocks.FIRE)) {
                            world.setBlockState(firePos, Blocks.AIR.getDefaultState());
                        }

                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player.getWorld().getRegistryKey() == World.NETHER) {
                removeFlintAndSteel(player);
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (world.isClient) return;
            if (entity instanceof ServerPlayerEntity player) {
                if (world.getRegistryKey() == World.NETHER) {
                    System.out.println("[NetherPortalControl] Player entered Nether — destroying nearby portals...");
                    destroyNearbyPortals(world, player.getBlockPos());
                    player.sendMessage(Text.of("§cПортал сломан! Победите Иссушителя, чтобы вернуться!"), false);
                    removeFlintAndSteel(player);
                }
            }
        });
    }

    private static void removeFlintAndSteel(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        Predicate<ItemStack> predicate = stack -> stack.getItem() == Items.FLINT_AND_STEEL;

        for (int i = 0; i < inventory.main.size(); i++) {
            if (predicate.test(inventory.main.get(i))) {
                inventory.removeStack(i);
            }
        }

        if (!inventory.offHand.isEmpty() && predicate.test(inventory.offHand.get(0))) {
            inventory.offHand.set(0, ItemStack.EMPTY);
        }
    }

    private static void destroyNearbyPortals(World world, BlockPos centerPos) {
        int radius = 10;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(centerPos.getX() + x, centerPos.getY() + y, centerPos.getZ() + z);
                    Block block = world.getBlockState(mutable).getBlock();
                    if (block == Blocks.NETHER_PORTAL) {
                        world.setBlockState(mutable, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }
}