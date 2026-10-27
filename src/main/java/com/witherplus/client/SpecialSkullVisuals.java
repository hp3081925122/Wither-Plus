package com.witherplus.client;

import com.witherplus.WitherPlus;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class SpecialSkullVisuals {
    public static final ResourceLocation TEXTURE = WitherPlus.id("textures/item/special_wither_skeleton.png");

    private SpecialSkullVisuals() {
    }

    public static RenderType renderType() {
        return RenderType.entityCutoutNoCullZOffset(TEXTURE);
    }
}
