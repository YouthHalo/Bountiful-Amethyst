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
		super(settings.maxDamage(16).rarity(net.minecraft.util.Rarity.EPIC)); // Set durability and blue rarity
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		
		// Check for cooldown (like ender pearls)
		if (user.getItemCooldownManager().isCoolingDown(itemStack)) {
			return ActionResult.FAIL;
		}

		// Play swinging animation for the hand holding the staff (on both sides)
		user.swingHand(hand);

		// Handle client-side logic
		if (world.isClient) {
			return ActionResult.SUCCESS; // Return success on client to show animation
		}

		// Server-side logic: spawn fireball
		// Get the player's look direction
		Vec3d lookDirection = user.getRotationVec(1.0F);

		// Calculate spawn position (0.5 blocks in front of player)
		Vec3d spawnPos = user.getPos().add(lookDirection.multiply(0.5));

		// Create and spawn the dragon fireball
		DragonFireballEntity fireball = new DragonFireballEntity(world, user, lookDirection) {
			@Override
			protected boolean canHit(net.minecraft.entity.Entity entity) {
				// Don't collide with the player who summoned this fireball
				if (entity == this.getOwner()) {
					return false;
				}
				return super.canHit(entity);
			}
			
			@Override
			protected void onCollision(net.minecraft.util.hit.HitResult hitResult) {
				super.onCollision(hitResult);
				if (!this.getWorld().isClient) {
					this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 0.5F, false, World.ExplosionSourceType.MOB);
					
					// Additional direct damage to nearby entities
					if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
						this.getWorld().getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
							this.getBoundingBox().expand(3.0), // 4 block radius
							entity -> true) // Damage ALL entities including the player
							.forEach(entity -> {
								// Deal different damage based on whether it's the owner
								float damage = entity == this.getOwner() ? 12.0F : 15.0F;
								entity.damage(serverWorld, this.getDamageSources().explosion(this, this.getOwner()), damage);
							});
					}
				}
			}
		};
		fireball.setPosition(spawnPos.x, spawnPos.y + user.getStandingEyeHeight(), spawnPos.z);
		fireball.setVelocity(lookDirection.multiply(2.0));

		world.spawnEntity(fireball);
		// Set cooldown for 2 seconds (40 ticks)
		user.getItemCooldownManager().set(itemStack, 40);

		// Handle durability damage manually to ensure item can break
		if (!user.getAbilities().creativeMode) {
			int currentDamage = itemStack.getDamage();
			int maxDamage = itemStack.getMaxDamage();
			
			// WHY DO I HAVE TO CODE IN ITEM DAMAGE BRO
			if (currentDamage + 1 >= maxDamage) {
				// Item will break - set stack to empty
				user.setStackInHand(hand, ItemStack.EMPTY);
				// Play break sound
				world.playSound(null, user.getX(), user.getY(), user.getZ(),
					net.minecraft.sound.SoundEvents.ENTITY_ITEM_BREAK,
					net.minecraft.sound.SoundCategory.PLAYERS,
					0.8F, 0.8F + world.random.nextFloat() * 0.4F);
			} else {
				// Item won't break - apply damage normally
				itemStack.setDamage(currentDamage + 1);
			}
		}

		return ActionResult.SUCCESS;
	}
}