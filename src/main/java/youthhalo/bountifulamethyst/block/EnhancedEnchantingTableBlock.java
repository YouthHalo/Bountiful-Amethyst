package youthhalo.bountifulamethyst.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EnhancedEnchantingTableBlock extends EnchantingTableBlock {

	public EnhancedEnchantingTableBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		// Safety check to prevent crashes
		if (world == null || pos == null || player == null) {
			return ActionResult.PASS;
		}
		
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else {
			player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			return ActionResult.CONSUME;
		}
	}

	@Override
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		// Safety check
		if (world == null || pos == null) {
			return null;
		}
		
		return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
			return new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)) {
				@Override
				public boolean canUse(PlayerEntity player) {
					return world.getBlockState(pos).getBlock() instanceof EnhancedEnchantingTableBlock &&
							player.squaredDistanceTo((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
									(double) pos.getZ() + 0.5D) <= 64.0D;
				}
			};
		}, Text.translatable("container.enchant"));
	}

	// Custom enchantment power calculation - each bookshelf gives 2 points instead
	// of 1
	public static int getEnhantingPower(World world, BlockPos pos) {
		// Safety check
		if (world == null || pos == null) {
			return 0;
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

				// Check for amethyst blocks in a 3x3 area under the table
				float amethystPower = 0;
				for (int dx = -1; dx <= 1; ++dx) {
					for (int dz = -1; dz <= 1; ++dz) {
						if (world.getBlockState(pos.add(dx, -1, dz)).isOf(Blocks.AMETHYST_BLOCK)) {
							amethystPower += 1.0f; // Amethyst blocks provide additional power
						}
					}
				}
				power += amethystPower;

		return MathHelper.clamp((int) power, 0, 15); // Max 15 for level 30 enchants
	}
}
