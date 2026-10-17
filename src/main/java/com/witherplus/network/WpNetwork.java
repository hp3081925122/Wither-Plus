package com.witherplus.network;

import com.witherplus.client.ClientVfxHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class WpNetwork {
    private WpNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(VfxPayload.TYPE, VfxPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientVfxHandler.handle(payload)));
    }

    public static void sendToPlayer(ServerPlayer player, VfxPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendNear(ServerLevel level, double x, double y, double z, double radius, VfxPayload payload) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) < radius * radius) {
                sendToPlayer(player, payload);
            }
        }
    }
}
