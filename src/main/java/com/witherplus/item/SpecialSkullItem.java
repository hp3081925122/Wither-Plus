package com.witherplus.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Blocks;

public class SpecialSkullItem extends StandingAndWallBlockItem {
    public SpecialSkullItem(Properties properties) {
        super(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, properties, Direction.DOWN);
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }
}
