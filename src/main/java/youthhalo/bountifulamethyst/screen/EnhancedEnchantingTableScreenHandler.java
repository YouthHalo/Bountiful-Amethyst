package youthhalo.bountifulamethyst.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import youthhalo.bountifulamethyst.util.EnchantingUtils;

public class EnhancedEnchantingTableScreenHandler extends ScreenHandler {
	private final Inventory inventory;
	private final ScreenHandlerContext context;

	// This constructor gets called on the client when the server wants it to open
	// the screenHandler,
	// The client will call the other constructor with an empty Inventory and the
	// screenHandler will automatically
	// sync this empty inventory with the inventory on the server.
	public EnhancedEnchantingTableScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(2), ScreenHandlerContext.EMPTY);
	}

	// This constructor gets called from the BlockEntity on the server without
	// calling the other constructor first,
	// the server knows the inventory of the container and can therefore directly
	// provide it as an argument.
	// This inventory will then be synced to the client.
	public EnhancedEnchantingTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
			ScreenHandlerContext context) {
		super(ModScreenHandlers.ENHANCED_ENCHANTING_TABLE, syncId);
		checkSize(inventory, 2);
		this.inventory = inventory;
		this.context = context;

		// Some inventories do custom logic when a player opens it.
		inventory.onOpen(playerInventory.player);

		// Enchanting table layout slots
		this.addSlot(new Slot(inventory, 0, 15, 47)); // Item slot (left)
		this.addSlot(new Slot(inventory, 1, 35, 47)); // Lapis slot (right)

		// The player inventory (moved down to match enchanting table)
		for (int m = 0; m < 3; ++m) {
			for (int l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
			}
		}
		// The player Hotbar
		for (int m = 0; m < 9; ++m) {
			this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot != null && slot.hasStack()) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();
			if (invSlot < this.inventory.size()) {
				if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
				return ItemStack.EMPTY;
			}

			if (originalStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}

		return newStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		this.dropInventory(player, this.inventory);
	}

	/**
	 * Get the XP cost for enchantment option 1 (equivalent to level 2 enchanting)
	 */
	public int getEnchantCost1() {
		return this.context.get((world, pos) -> EnchantingUtils.getRevealCost(world, pos, 2)).orElse(10);
	}

	/**
	 * Get the XP cost for enchantment option 2 (equivalent to level 3 enchanting)
	 */
	public int getEnchantCost2() {
		return this.context.get((world, pos) -> EnchantingUtils.getRevealCost(world, pos, 3)).orElse(15);
	}

	/**
	 * Get the XP cost to reveal what enchantments will be applied
	 */
	public int getRevealCost() {
		return this.context.get((world, pos) -> EnchantingUtils.getRevealCost(world, pos, 1)).orElse(5);
	}

	/**
	 * Get the number of amethyst blocks underneath the table
	 */
	public int getAmethystBlockCount() {
		return this.context.get((world, pos) -> EnchantingUtils.getAmethystBlockCount(world, pos)).orElse(0);
	}
}
