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

		// Calculate spawn position (0.1 blocks in front of player)
		Vec3d spawnPos = user.getPos().add(lookDirection.multiply(0.1));

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
				// Don't call super.onCollision() to prevent vanilla area effect cloud
				if (!this.getWorld().isClient) {
					this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 0.5F, false, World.ExplosionSourceType.MOB);
					
					// Additional direct damage to nearby entities from explosion
					if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
						this.getWorld().getEntitiesByClass(net.minecraft.entity.Entity.class,
							this.getBoundingBox().expand(4.0), // 4 block radius for explosion damage
							entity -> entity instanceof net.minecraft.entity.LivingEntity) // Target all living entities
							.forEach(entity -> {
								// Deal different damage based on whether it's the owner
								float damage = entity == this.getOwner() ? 12.0F : 15.0F;
								((net.minecraft.entity.LivingEntity) entity).damage(serverWorld, 
									serverWorld.getDamageSources().explosion(this, this.getOwner()), damage);
							});
					}
					
					// Create custom area effect cloud that affects ALL entities
					// Find the ground level by checking for solid blocks below
					double groundY = this.getY();
					for (int i = 0; i < 20; i++) { // Check up to 20 blocks down
						net.minecraft.util.math.BlockPos checkPos = new net.minecraft.util.math.BlockPos((int)this.getX(), (int)(groundY - i), (int)this.getZ());
						if (this.getWorld().getBlockState(checkPos).isSolidBlock(this.getWorld(), checkPos)) {
							groundY = checkPos.getY() + 1; // Position 1 block above the solid ground
							break;
						}
					}
					
					net.minecraft.entity.AreaEffectCloudEntity areaEffectCloud = new net.minecraft.entity.AreaEffectCloudEntity(this.getWorld(), this.getX(), groundY, this.getZ()) {
						@Override
						public void tick() {
							super.tick();
							if (!this.getWorld().isClient && this.isAlive()) {
								// Custom damage logic for all entities in range
								java.util.List<net.minecraft.entity.Entity> entities = this.getWorld().getOtherEntities(this, this.getBoundingBox());
								for (net.minecraft.entity.Entity entity : entities) {
									if (entity instanceof net.minecraft.entity.LivingEntity livingEntity) {
										// Deal damage to all living entities including undead
										float damage = entity == this.getOwner() ? 2.0F : 3.0F;
										if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
											livingEntity.damage(serverWorld, serverWorld.getDamageSources().magic(), damage);
										}
									}
								}
							}
						}
					};
					
					// Set owner only if it's a LivingEntity
					if (this.getOwner() instanceof net.minecraft.entity.LivingEntity livingOwner) {
						areaEffectCloud.setOwner(livingOwner);
					}
					areaEffectCloud.setParticleType(net.minecraft.particle.ParticleTypes.DRAGON_BREATH);
					areaEffectCloud.setRadius(3.0F);
					areaEffectCloud.setDuration(600); // 30 seconds
					areaEffectCloud.setRadiusOnUse(-0.5F); // Shrinks when dealing damage
					areaEffectCloud.setWaitTime(10); // Delay before starting to shrink
					
					this.getWorld().spawnEntity(areaEffectCloud);
				}
				this.discard();
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