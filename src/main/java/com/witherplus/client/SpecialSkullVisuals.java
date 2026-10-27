package com.witherplus.client;

import com.witherplus.WitherPlus;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

public final class SpecialSkullVisuals {
    public static final Identifier TEXTURE = WitherPlus.id("textures/item/special_wither_skeleton.png");
    public static final ContextKey<Boolean> WORN = new ContextKey<>(WitherPlus.id("special_skull_worn"));

    private SpecialSkullVisuals() {}

    public static RenderType renderType() {
        return RenderTypes.entityCutoutZOffset(TEXTURE);
    }
}
