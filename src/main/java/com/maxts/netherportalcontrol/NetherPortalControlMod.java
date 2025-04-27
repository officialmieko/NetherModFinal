package com.maxts.netherportalcontrol;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class NetherPortalControlMod implements ModInitializer {

    public static boolean witherDefeated = false;
    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        System.out.println("[NetherPortalControl] Mod initializing...");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            NetherPortalControlMod.server = server;
            witherDefeated = false;
            System.out.println("[NetherPortalControl] Server starting — reset witherDefeated = false.");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            NetherPortalControlMod.server = null;
            System.out.println("[NetherPortalControl] Server stopped — cleared server reference.");
        });

        PortalEventHandler.register();
        WitherEventHandler.register();
    }
}