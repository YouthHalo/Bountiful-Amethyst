package youthhalo.bountifulamethyst.util;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import youthhalo.bountifulamethyst.block.EnhancedEnchantingTableBlock;

public class EnchantingUtils {

    /**
     * Calculates enhanced enchanting power for Enhanced Enchanting Tables
     * Each bookshelf provides 2 power instead of the vanilla 1 power
     * 
     * @param world The world
     * @param pos The position of the enchanting table
     * @return Enhanced power value (0-15)
     */
    public static int getEnhancedPower(World world, BlockPos pos) {
        // Check if this is our enhanced enchanting table
        if (!(world.getBlockState(pos).getBlock() instanceof EnhancedEnchantingTableBlock)) {
            return 0; // Not an enhanced enchanting table, return 0
        }

        float power = 0;
        
        // Check for bookshelves in a 5x5x2 area around the enchanting table
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                if ((dx != 0 || dz != 0) && world.isAir(pos.add(dx, 0, dz)) && world.isAir(pos.add(dx, 1, dz))) {
                    // Check level 0 (same level as enchanting table)
                    if (world.getBlockState(pos.add(dx, 0, dz)).isOf(Blocks.BOOKSHELF)) {
                        power += 2.0f; // Enhanced: 2 points per bookshelf instead of 1
                    }
                    // Check level 1 (one block above enchanting table)
                    if (world.getBlockState(pos.add(dx, 1, dz)).isOf(Blocks.BOOKSHELF)) {
                        power += 2.0f; // Enhanced: 2 points per bookshelf instead of 1
                    }
                }
            }
        }
        
        return MathHelper.clamp((int)power, 0, 15); // Max 15 for level 30 enchants
    }
}
