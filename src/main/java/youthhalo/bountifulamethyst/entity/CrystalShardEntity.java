package youthhalo.bountifulamethyst.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class CrystalShardEntity extends ThrownItemEntity {
    
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
                if (entityHit.getEntity() instanceof net.minecraft.entity.LivingEntity livingEntity) {
                    if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                        livingEntity.damage(serverWorld,
                                serverWorld.getDamageSources().thrown(this, this.getOwner()), 8.0F);
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
