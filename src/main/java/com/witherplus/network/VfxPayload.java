package com.witherplus.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record VfxPayload(int kind, int entityId, CompoundTag data) {
    public static final int STAGE = 0;
    public static final int PYRE = 1;
    public static final int METEOR = 2;
    public static final int CHAIN = 3;
    public static final int CHAIN_END = 4;
    public static final int SHADE = 5;
    public static final int DASH = 6;
    public static final int SPIRES = 7;
    public static final int BLACK_SUN = 8;
    public static final int BOLTS = 9;
    public static final int MAELSTROM = 10;
    public static final int MARK = 11;
    public static final int PILLAR = 12;
    public static final int TIDE = 13;
    public static final int CROWN = 14;
    public static final int REQUIEM = 15;
    public static final int BURST = 16;

    public static void encode(VfxPayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.kind());
        buffer.writeVarInt(payload.entityId());
        buffer.writeNbt(payload.data());
    }

    public static VfxPayload decode(FriendlyByteBuf buffer) {
        int kind = buffer.readVarInt();
        int entityId = buffer.readVarInt();
        CompoundTag data = buffer.readNbt();
        if (data == null) {
            data = new CompoundTag();
        }
        return new VfxPayload(kind, entityId, data);
    }

    public static void putVec(CompoundTag tag, String key, Vec3 vec) {
        tag.putDouble(key + "X", vec.x);
        tag.putDouble(key + "Y", vec.y);
        tag.putDouble(key + "Z", vec.z);
    }

    public static Vec3 getVec(CompoundTag tag, String key) {
        return new Vec3(tag.getDouble(key + "X"), tag.getDouble(key + "Y"), tag.getDouble(key + "Z"));
    }

    public static void putPoints(CompoundTag tag, String key, List<Vec3> points) {
        ListTag list = new ListTag();
        for (Vec3 point : points) {
            list.add(DoubleTag.valueOf(point.x));
            list.add(DoubleTag.valueOf(point.y));
            list.add(DoubleTag.valueOf(point.z));
        }
        tag.put(key, list);
    }

    public static List<Vec3> getPoints(CompoundTag tag, String key) {
        ListTag list = tag.getList(key, Tag.TAG_DOUBLE);
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i + 2 < list.size(); i += 3) {
            points.add(new Vec3(list.getDouble(i), list.getDouble(i + 1), list.getDouble(i + 2)));
        }
        return points;
    }
}
