package youthhalo.bountifulamethyst.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AmethystStaffItem extends Item {
    
    public AmethystStaffItem(Settings settings) {
        super(settings.maxDamage(16)); // Set durability in constructor
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        // Check for cooldown (like ender pearls)
        if (user.getItemCooldownManager().isCoolingDown(itemStack)) {
            return ActionResult.FAIL;
        }
        
        // Ensure we don't spawn the fireball only on the client to prevent desync
        if (world.isClient) {
            return ActionResult.PASS;
        }

        // Get the player's look direction
        Vec3d lookDirection = user.getRotationVec(1.0F);
        
        // Calculate spawn position (2 blocks in front of player)
        Vec3d spawnPos = user.getPos().add(lookDirection.multiply(2.0));
        
        // Create and spawn the dragon fireball
        DragonFireballEntity fireball = new DragonFireballEntity(world, user, lookDirection);
        fireball.setPosition(spawnPos.x, spawnPos.y + user.getStandingEyeHeight(), spawnPos.z);
        
        world.spawnEntity(fireball);

        // Set cooldown for 2 seconds (40 ticks)
        user.getItemCooldownManager().set(itemStack, 40);

        // Damage the item (reduce durability by 1)
        itemStack.damage(1, user);

        return ActionResult.SUCCESS;
    }
}
