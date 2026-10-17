package com.witherplus.network;

import com.witherplus.WitherPlus;
import com.witherplus.client.ClientVfxHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class WpNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(WitherPlus.id("vfx"), () -> "1", "1"::equals, "1"::equals);

    private WpNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(VfxPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VfxPayload::encode).decoder(VfxPayload::decode)
                .consumerMainThread((payload, context) -> ClientVfxHandler.handle(payload)).add();
    }

    public static void sendToPlayer(ServerPlayer player, VfxPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendNear(ServerLevel level, double x, double y, double z, double radius, VfxPayload payload) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) < radius * radius) {
                sendToPlayer(player, payload);
            }
        }
    }
}
