package youthhalo.bountifulamethyst.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import youthhalo.bountifulamethyst.entity.CrystalShardEntity;

public class AmethystTomeItem extends Item {

	public AmethystTomeItem(Settings settings) {
		super(settings.maxDamage(64).rarity(net.minecraft.util.Rarity.UNCOMMON));
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		// Check for cooldown
		if (user.getItemCooldownManager().isCoolingDown(itemStack)) {
			return ActionResult.FAIL;
		}

		// Play swinging animation
		user.swingHand(hand);

		// Handle client-side logic
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		// Server-side logic: sequential crystal shard burst
		Vec3d lookDirection = user.getRotationVec(1.0F);

		// Fire 5 crystal shards one at a time with delays
		if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
			for (int i = 0; i < 5; i++) {
				final int shardIndex = i;
				final Vec3d capturedLookDirection = lookDirection;

				// Schedule each shard to fire with increasing delay using a simple
				// counter-based approach
				new Thread(() -> {
					try {
						Thread.sleep(shardIndex * 50); // 0.1 second delay between each shot

						// Execute on server thread
						serverWorld.getServer().execute(() -> {
							// Calculate spread based on shot number (0-indexed)
							// Shot 0: 0 spread (perfect accuracy)
							// Shot 1: 25% of max spread
							// Shot 2: 50% of max spread
							// Shot 3: 75% of max spread (4th shot)
							// Shot 4: 100% of max spread (5th shot)
							double maxSpread = 0.33;
							double spread = (shardIndex / 4.0) * maxSpread; // Gradually increase from 0 to maxSpread

							Vec3d direction;
							if (shardIndex == 0) {
								// First shot: perfect accuracy, no spread
								direction = capturedLookDirection;
							} else {
								// Subsequent shots: increasing spread
								direction = new Vec3d(
										capturedLookDirection.x + (world.random.nextDouble() - 0.5) * spread,
										capturedLookDirection.y + (world.random.nextDouble() - 0.5) * spread * 0.5,
										capturedLookDirection.z + (world.random.nextDouble() - 0.5) * spread).normalize();
							}

							// Create crystal shard projectile
							CrystalShardEntity shard = new CrystalShardEntity(world, user);

							// Position slightly in front of player
							Vec3d spawnPos = user.getPos().add(
									direction.x * 1.0,
									user.getStandingEyeHeight(),
									direction.z * 1.0);

							shard.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
							shard.setVelocity(direction.multiply(2.0)); // Consistent speed for better aim

							world.spawnEntity(shard);

							// Play firing sound for each shard
							world.playSound(null, user.getX(), user.getY(), user.getZ(),
									net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT,
									net.minecraft.sound.SoundCategory.PLAYERS,
									0.3F, 1.0F + shardIndex * 0.1F); // Slightly different pitch for each
						});
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}).start();
			}
		}

		// Set cooldown for 1 second (20 ticks)
		user.getItemCooldownManager().set(itemStack, 40);

		// Handle durability damage manually fml
		if (!user.getAbilities().creativeMode) {
			int currentDamage = itemStack.getDamage();
			int maxDamage = itemStack.getMaxDamage();

			if (currentDamage + 1 >= maxDamage) {
				// Item will break
				user.setStackInHand(hand, ItemStack.EMPTY);
				world.playSound(null, user.getX(), user.getY(), user.getZ(),
						net.minecraft.sound.SoundEvents.ENTITY_ITEM_BREAK,
						net.minecraft.sound.SoundCategory.PLAYERS,
						0.8F, 0.8F + world.random.nextFloat() * 0.4F);
			} else {
				itemStack.setDamage(currentDamage + 1);
			}
		}

		return ActionResult.SUCCESS;
	}
}
