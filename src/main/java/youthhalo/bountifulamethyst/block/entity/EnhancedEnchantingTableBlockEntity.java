package youthhalo.bountifulamethyst.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import youthhalo.bountifulamethyst.screen.EnhancedEnchantingTableScreenHandler;

public class EnhancedEnchantingTableBlockEntity extends BlockEntity
		implements NamedScreenHandlerFactory, ImplementedInventory {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

	public EnhancedEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ENHANCED_ENCHANTING_TABLE, pos, state);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return inventory;
	}

	// TODO: Add NBT persistence later for saving/loading inventory

	@Override
	public Text getDisplayName() {
		return Text.translatable("container.enhanced_enchanting_table");
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new EnhancedEnchantingTableScreenHandler(syncId, playerInventory, this, 
			net.minecraft.screen.ScreenHandlerContext.create(world, pos));
	}

	public static void tick(World world, BlockPos pos, BlockState state, EnhancedEnchantingTableBlockEntity blockEntity) {
		// Add any tick logic here if needed
	}
}
