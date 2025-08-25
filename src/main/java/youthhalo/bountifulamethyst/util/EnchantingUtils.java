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

    /**
     * Counts amethyst blocks underneath the Enhanced Enchanting Table
     * Used to reduce reveal costs
     * 
     * @param world The world
     * @param pos The position of the enchanting table
     * @return Number of amethyst blocks found (0-9 for a 3x3 area)
     */
    public static int getAmethystBlockCount(World world, BlockPos pos) {
        if (!(world.getBlockState(pos).getBlock() instanceof EnhancedEnchantingTableBlock)) {
            return 0;
        }

        int count = 0;
        
        // Check 3x3 area underneath the enchanting table
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                BlockPos checkPos = pos.add(dx, -1, dz);
                if (world.getBlockState(checkPos).isOf(Blocks.AMETHYST_BLOCK)) {
                    count++;
                }
            }
        }
        
        return count;
    }

    /**
     * Calculates the reveal cost for enchantment options
     * Base costs are 5, 10, 15 XP, reduced by amethyst blocks underneath
     * 
     * @param world The world
     * @param pos The position of the enchanting table
     * @param tier The enchantment tier (1, 2, or 3)
     * @return XP cost for revealing this tier
     */
    public static int getRevealCost(World world, BlockPos pos, int tier) {
        int baseCost = tier * 5; // 5, 10, 15 XP for tiers 1, 2, 3
        int amethystBlocks = getAmethystBlockCount(world, pos);
        
        // Each amethyst block reduces cost by 1 XP, minimum cost of 1
        int reduction = amethystBlocks;
        return Math.max(1, baseCost - reduction);
    }

    /**
     * Calculates the effective bookshelf power needed for enchanting
     * Enhanced enchanting tables need half as many books for the same enchantment strength
     * 
     * @param regularPower The power that would be needed for a regular enchanting table
     * @return Required power for enhanced enchanting table (halved, minimum 1)
     */
    public static int getRequiredEnhancedPower(int regularPower) {
        return Math.max(1, regularPower / 2);
    }
}
