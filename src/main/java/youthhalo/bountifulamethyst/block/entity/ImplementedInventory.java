package youthhalo.bountifulamethyst.block.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * A simple {@code Inventory} implementation with only default methods + an item
 * list getter.
 */
public interface ImplementedInventory extends Inventory {
	/**
	 * Gets the item list of this inventory.
	 * Must return the same instance every time it's called.
	 */
	DefaultedList<ItemStack> getItems();

	/**
	 * Creates an inventory from the item list.
	 */
	static ImplementedInventory of(DefaultedList<ItemStack> items) {
		return () -> items;
	}

	/**
	 * Creates a new inventory with the size.
	 */
	static ImplementedInventory ofSize(int size) {
		return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
	}

	// Implementation of Inventory

	@Override
	default int size() {
		return getItems().size();
	}

	@Override
	default boolean isEmpty() {
		for (int i = 0; i < size(); i++) {
			ItemStack stack = getStack(i);
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	default ItemStack getStack(int slot) {
		return getItems().get(slot);
	}

	@Override
	default ItemStack removeStack(int slot, int count) {
		ItemStack result = Inventories.splitStack(getItems(), slot, count);
		if (!result.isEmpty()) {
			markDirty();
		}
		return result;
	}

	@Override
	default ItemStack removeStack(int slot) {
		return Inventories.removeStack(getItems(), slot);
	}

	@Override
	default void setStack(int slot, ItemStack stack) {
		getItems().set(slot, stack);
		if (stack.getCount() > getMaxCountPerStack()) {
			stack.setCount(getMaxCountPerStack());
		}
	}

	@Override
	default void clear() {
		getItems().clear();
	}

	@Override
	default void markDirty() {
		// Override if you want behavior.
	}

	@Override
	default boolean canPlayerUse(PlayerEntity player) {
		return true;
	}
}
