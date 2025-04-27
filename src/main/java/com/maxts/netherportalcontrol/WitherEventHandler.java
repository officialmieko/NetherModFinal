package com.maxts.netherportalcontrol;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld; // Import needed

public class WitherEventHandler {

    public static void register() {
        System.out.println("[NetherPortalControl] WitherEventHandler registering events...");

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.getWorld() instanceof ServerWorld)) return; // Server only
            if (entity instanceof WitherEntity) {
                NetherPortalControlMod.witherDefeated = true;
                System.out.println("[NetherPortalControl] Wither defeated! Portals can now be reactivated.");

                if (NetherPortalControlMod.server != null) {
                    for (ServerPlayerEntity player : NetherPortalControlMod.server.getPlayerManager().getPlayerList()) {
                        player.sendMessage(
                            net.minecraft.text.Text.of("§aВы победили Иссушителя! Теперь вы можете активировать портал в Оверворлд."),
                            false
                        );
                    }
                }
            }
        });
    }
}