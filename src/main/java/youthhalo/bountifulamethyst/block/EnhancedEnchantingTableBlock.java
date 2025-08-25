package youthhalo.bountifulamethyst.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import youthhalo.bountifulamethyst.block.entity.EnhancedEnchantingTableBlockEntity;

public class EnhancedEnchantingTableBlock extends BlockWithEntity {
	
	public static final MapCodec<EnhancedEnchantingTableBlock> CODEC = createCodec(EnhancedEnchantingTableBlock::new);

	public EnhancedEnchantingTableBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Override
	public MapCodec<EnhancedEnchantingTableBlock> getCodec() {
		return CODEC;
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
		if (!world.isClient) {
			NamedScreenHandlerFactory screenHandlerFactory = (NamedScreenHandlerFactory) world.getBlockEntity(pos);
			if (screenHandlerFactory != null) {
				player.openHandledScreen(screenHandlerFactory);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EnhancedEnchantingTableBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
