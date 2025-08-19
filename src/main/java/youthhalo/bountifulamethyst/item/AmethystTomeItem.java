package youthhalo.bountifulamethyst.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AmethystTomeItem extends Item {

	public AmethystTomeItem(Settings settings) {
		super(settings.maxDamage(64).rarity(net.minecraft.util.Rarity.RARE));
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

		// Server-side logic: rapidly summon crystal shards
		Vec3d playerPos = user.getPos();
		Vec3d lookDirection = user.getRotationVec(1.0F);

		// Summon 5 crystal shards in rapid succession
		for (int i = 0; i < 5; i++) {
			// Create slight variations in direction for spread
			double spread = 0.3;
			Vec3d direction = new Vec3d(
					lookDirection.x + (world.random.nextDouble() - 0.5) * spread,
					lookDirection.y + (world.random.nextDouble() - 0.5) * spread * 0.5,
					lookDirection.z + (world.random.nextDouble() - 0.5) * spread).normalize();

			// Create crystal shard projectile
			CrystalShardEntity shard = new CrystalShardEntity(world, user);

			// Position slightly in front of player with small offset
			Vec3d spawnPos = playerPos.add(
					direction.x * 1.5 + (world.random.nextDouble() - 0.5) * 0.5,
					user.getStandingEyeHeight() + (world.random.nextDouble() - 0.5) * 0.3,
					direction.z * 1.5 + (world.random.nextDouble() - 0.5) * 0.5);

			shard.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
			shard.setVelocity(direction.multiply(1.5 + world.random.nextDouble() * 0.5));

			world.spawnEntity(shard);
		}

		// Set cooldown for 1 second (20 ticks)
		user.getItemCooldownManager().set(itemStack, 20);

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

	// Inner class for Crystal Shard Entity
	public static class CrystalShardEntity extends ThrownItemEntity {
		public CrystalShardEntity(net.minecraft.entity.EntityType<? extends ThrownItemEntity> entityType, World world) {
			super(entityType, world);
		}

		public CrystalShardEntity(World world, PlayerEntity owner) {
			super(net.minecraft.entity.EntityType.SNOWBALL, world); // Use snowball entity type as base
			this.setOwner(owner);
		}

		@Override
		protected Item getDefaultItem() {
			return net.minecraft.item.Items.AMETHYST_SHARD;
		}

		@Override
		protected void onCollision(net.minecraft.util.hit.HitResult hitResult) {
			super.onCollision(hitResult);

			if (!this.getWorld().isClient) {
				// Deal damage to entities hit
				if (hitResult instanceof net.minecraft.util.hit.EntityHitResult entityHit) {
					if (entityHit.getEntity() instanceof net.minecraft.entity.LivingEntity livingEntity &&
							entityHit.getEntity() != this.getOwner()) {

						if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
							livingEntity.damage(serverWorld,
									serverWorld.getDamageSources().thrown(this, this.getOwner()), 6.0F);
						}
					}
				}

				// Create crystal break particles and sound
				this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
						net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK,
						net.minecraft.sound.SoundCategory.NEUTRAL,
						0.5F, 1.2F + this.getWorld().random.nextFloat() * 0.5F);

				// Spawn amethyst break particles
				if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
					serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT,
							this.getX(), this.getY(), this.getZ(),
							8, 0.2, 0.2, 0.2, 0.1);
				}

				this.discard();
			}
		}
	}
}
