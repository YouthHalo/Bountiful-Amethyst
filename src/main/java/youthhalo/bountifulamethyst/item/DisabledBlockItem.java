package youthhalo.bountifulamethyst.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * A special BlockItem for the Enhanced Enchanting Table that prevents crashes
 * when used as an item, but allows normal block placement.
 */
public class DisabledBlockItem extends BlockItem {

    public DisabledBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // Block all usage in air to prevent crash
        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Safely call the parent method to allow placement
        try {
            return super.useOnBlock(context);
        } catch (Exception e) {
            // If an error occurs, log it and fail safely
            if (!context.getWorld().isClient) {
                System.err.println("Error when placing Enhanced Enchanting Table: " + e.getMessage());
            }
            return ActionResult.FAIL;
        }
    }
}
