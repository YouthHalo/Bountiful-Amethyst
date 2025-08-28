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
	private boolean revealMode = false;
	private java.util.List<net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment>> selectedEnchantments = new java.util.ArrayList<>();

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

		// Add property for reveal mode
		this.addProperty(new net.minecraft.screen.Property() {
			@Override
			public int get() {
				return revealMode ? 1 : 0;
			}
			
			@Override
			public void set(int value) {
				revealMode = (value == 1);
			}
		});

		// Enchanting table layout slots
		this.addSlot(new Slot(inventory, 0, 15, 47)); // Item slot (left)
		this.addSlot(new Slot(inventory, 1, 35, 47) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return stack.getItem() == net.minecraft.item.Items.LAPIS_LAZULI;
			}
		}); // Lapis slot (right)

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
		// Reset reveal mode when closing
		resetRevealMode();
	}

	/**
	 * Reset reveal mode and clear selected enchantments
	 */
	private void resetRevealMode() {
		revealMode = false;
		selectedEnchantments.clear();
		this.sendContentUpdates();
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

	/**
	 * Check if reveal mode is active
	 */
	public boolean isRevealMode() {
		return revealMode;
	}

	/**
	 * Get the selected enchantments for preview
	 */
	public java.util.List<net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment>> getSelectedEnchantments() {
		return selectedEnchantments;
	}

	/**
	 * Check if an item can be enchanted (tools, weapons, armor, etc.)
	 * Only allows items WITHOUT existing enchantments
	 */
	private static boolean isEnchantableItem(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		}
		
		// Check if item already has enchantments - if so, don't allow enchanting
		var existingEnchantments = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(itemStack);
		if (!existingEnchantments.isEmpty()) {
			return false;
		}
		
		var item = itemStack.getItem();
		
		// Check for common enchantable items by their string ID
		String itemId = net.minecraft.registry.Registries.ITEM.getId(item).toString();
		
		return itemId.contains("sword") ||
			   itemId.contains("pickaxe") ||
			   itemId.contains("axe") ||
			   itemId.contains("shovel") ||
			   itemId.contains("hoe") ||
			   itemId.contains("helmet") ||
			   itemId.contains("chestplate") ||
			   itemId.contains("leggings") ||
			   itemId.contains("boots") ||
			   itemId.contains("bow") ||
			   itemId.contains("crossbow") ||
			   itemId.contains("trident") ||
			   itemId.contains("fishing_rod") ||
			   itemId.contains("shears") ||
			   itemId.contains("flint_and_steel") ||
			   item == net.minecraft.item.Items.BOOK ||
			   itemId.contains("shield") ||
			   itemId.contains("elytra");
	}

	/**
	 * Handle button clicks for enchanting
	 */
	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		return this.context.get((world, pos) -> {
			ItemStack itemStack = this.getSlot(0).getStack();
			ItemStack lapisStack = this.getSlot(1).getStack();
			
			// Check if there's an enchantable item
			if (itemStack.isEmpty() || !isEnchantableItem(itemStack)) {
				return false;
			}
			
			// Check lapis requirements
			int requiredLapis = (id == 0) ? 2 : (id == 1) ? 3 : 1;
			if (lapisStack.isEmpty() || lapisStack.getItem() != net.minecraft.item.Items.LAPIS_LAZULI || lapisStack.getCount() < requiredLapis) {
				return false;
			}
			
			int xpCost = (id == 0) ? this.getEnchantCost1() : 
						(id == 1) ? this.getEnchantCost2() : 
						this.getRevealCost();
			
			// Check if player has enough XP
			if (player.experienceLevel < xpCost && !player.isInCreativeMode()) {
				return false;
			}
			
			// For the third button (reveal), select enchantments and enter reveal mode
			if (id == 2) {
				if (!player.isInCreativeMode()) {
					player.addExperienceLevels(-xpCost);
					lapisStack.decrement(requiredLapis);
				}
				
				// Select enchantments for preview (same logic as actual enchanting)
				int power = EnchantingUtils.getEnhancedPower(world, pos);
				int enchantLevel = 2; // Use level 2 as base for selection
				
				var enchantmentRegistry = world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
				var possibleEnchantments = enchantmentRegistry.streamEntries()
					.filter(entry -> entry.value().isAcceptableItem(itemStack))
					.filter(entry -> entry.value().getMinLevel() <= enchantLevel && entry.value().getMaxLevel() >= enchantLevel)
					.toList();
				
				if (!possibleEnchantments.isEmpty()) {
					var random = net.minecraft.util.math.random.Random.create();
					long seed = world.getTime() + pos.hashCode() + itemStack.hashCode();
					random.setSeed(seed);
					
					int numEnchantments = power >= 10 ? 2 : 1;
					numEnchantments = Math.min(numEnchantments, possibleEnchantments.size());
					
					selectedEnchantments.clear();
					var shuffledEnchantments = new java.util.ArrayList<>(possibleEnchantments);
					java.util.Collections.shuffle(shuffledEnchantments, new java.util.Random(random.nextLong()));
					
					for (int i = 0; i < numEnchantments; i++) {
						selectedEnchantments.add(shuffledEnchantments.get(i));
					}
					
					revealMode = true;
					this.sendContentUpdates();
				}
				
				return true;
			}
			
			// For enchanting buttons, use random vanilla enchantments
			int power = EnchantingUtils.getEnhancedPower(world, pos);
			
			// Allow enchanting even with 0 power (minimum requirements)
			if (power >= 0) {
				// Generate random enchantments appropriate for the item
				int enchantLevel = (id == 0) ? 2 : 3;
				
				// Get enchantment registry
				var enchantmentRegistry = world.getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
				
				// Get all possible enchantments for this item
				var possibleEnchantments = enchantmentRegistry.streamEntries()
					.filter(entry -> entry.value().isAcceptableItem(itemStack))
					.filter(entry -> entry.value().getMinLevel() <= enchantLevel && entry.value().getMaxLevel() >= enchantLevel)
					.toList();
				
				if (!possibleEnchantments.isEmpty()) {
					// Use selected enchantments if in reveal mode, otherwise select new ones
					var enchantmentsToApply = new java.util.ArrayList<net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment>>();
					
					if (revealMode && !selectedEnchantments.isEmpty()) {
						// Use the pre-selected enchantments
						enchantmentsToApply.addAll(selectedEnchantments);
					} else {
						// Select new enchantments
						var random = net.minecraft.util.math.random.Random.create();
						long seed = world.getTime() + pos.hashCode() + itemStack.hashCode();
						random.setSeed(seed);
						
						int numEnchantments = power >= 10 ? 2 : 1;
						numEnchantments = Math.min(numEnchantments, possibleEnchantments.size());
						
						var shuffledEnchantments = new java.util.ArrayList<>(possibleEnchantments);
						java.util.Collections.shuffle(shuffledEnchantments, new java.util.Random(random.nextLong()));
						
						for (int i = 0; i < numEnchantments; i++) {
							enchantmentsToApply.add(shuffledEnchantments.get(i));
						}
					}
					
					// Apply the enchantments
					var currentEnchantments = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(itemStack);
					var enchantmentBuilder = new net.minecraft.component.type.ItemEnchantmentsComponent.Builder(currentEnchantments);
					
					for (var enchantment : enchantmentsToApply) {
						enchantmentBuilder.add(enchantment, enchantLevel);
					}
					
					var newEnchantments = enchantmentBuilder.build();
					net.minecraft.enchantment.EnchantmentHelper.set(itemStack, newEnchantments);
					
					// Reset reveal mode after enchanting
					revealMode = false;
					selectedEnchantments.clear();
					this.sendContentUpdates();
					
					// Update the item and consume resources
					this.getSlot(0).setStack(itemStack);
					this.getSlot(0).markDirty();
					
					if (!player.isInCreativeMode()) {
						player.addExperienceLevels(-xpCost);
						lapisStack.decrement(requiredLapis);
						this.getSlot(1).setStack(lapisStack);
						this.getSlot(1).markDirty();
					}
					
					this.sendContentUpdates();
					player.getInventory().markDirty();
					
					return true;
				}
			}
			
			return false;
		}).orElse(false);
	}
}
