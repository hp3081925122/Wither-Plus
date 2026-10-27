package com.witherplus.item;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SpecialSkullItem extends StandingAndWallBlockItem {
    public SpecialSkullItem(Properties properties) {
        super(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Direction.DOWN, properties);
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
    }
}
