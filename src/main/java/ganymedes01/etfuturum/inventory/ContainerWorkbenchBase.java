package ganymedes01.etfuturum.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Shared logic for the stonecutter, loom, cartography table and fletching table: input slots that are given back
 * when the GUI closes, a single result slot, and the player inventory.
 */
public abstract class ContainerWorkbenchBase extends Container {

	protected final World world;
	protected final int x, y, z;
	private final Block block;
	protected final InventoryCraftResult resultInventory = new InventoryCraftResult();
	protected int resultSlotIndex = -1;
	protected int playerInvStart = -1;

	protected ContainerWorkbenchBase(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.block = world.getBlock(x, y, z);
	}

	protected abstract IInventory getInputInventory();

	/**
	 * Consume the inputs after the player took the result.
	 */
	protected abstract void onTakeResult(EntityPlayer player, ItemStack result);

	/**
	 * Last-moment, server-side changes to the result before the player gets it (e.g. allocating a new map ID).
	 * Must be idempotent: it can run twice for one craft.
	 */
	protected void finalizeResult(EntityPlayer player, ItemStack result) {
	}

	/**
	 * Try to move a stack from the player inventory into the input slots.
	 */
	protected abstract boolean mergeIntoInputs(ItemStack stack);

	protected Slot addResultSlot(int sx, int sy) {
		resultSlotIndex = inventorySlots.size();
		return addSlotToContainer(new Slot(resultInventory, 0, sx, sy) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			@Override
			public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
				finalizeResult(player, stack);
				stack.onCrafting(world, player, stack.stackSize);
				onTakeResult(player, stack);
				super.onPickupFromSlot(player, stack);
			}
		});
	}

	protected void addPlayerInventory(InventoryPlayer inv) {
		playerInvStart = inventorySlots.size();
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 9; col++)
				addSlotToContainer(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
		for (int col = 0; col < 9; col++)
			addSlotToContainer(new Slot(inv, col, 8 + col * 18, 142));
	}

	protected void playSound(String sound) {
		if (!world.isRemote) {
			world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, sound, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return world.getBlock(x, y, z) == block && player.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D) <= 64.0D;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!world.isRemote) {
			IInventory inputs = getInputInventory();
			for (int i = 0; i < inputs.getSizeInventory(); i++) {
				ItemStack stack = inputs.getStackInSlotOnClosing(i);
				if (stack != null && !player.inventory.addItemStackToInventory(stack)) {
					player.dropPlayerItemWithRandomChoice(stack, false);
				}
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		Slot slot = (Slot) inventorySlots.get(index);
		if (slot == null || !slot.getHasStack()) return null;
		ItemStack stack = slot.getStack();
		ItemStack copy = stack.copy();
		int playerInvEnd = playerInvStart + 36;

		if (index == resultSlotIndex) {
			// Container.slotClick repeats this while the result keeps being the same item, crafting a whole stack
			finalizeResult(player, stack);
			copy = stack.copy();
			if (!mergeItemStack(stack, playerInvStart, playerInvEnd, true)) return null;
			slot.onSlotChange(stack, copy);
		} else if (index < playerInvStart) {
			if (!mergeItemStack(stack, playerInvStart, playerInvEnd, false)) return null;
		} else if (!mergeIntoInputs(stack)) {
			if (index < playerInvStart + 27) {
				if (!mergeItemStack(stack, playerInvStart + 27, playerInvEnd, false)) return null;
			} else if (!mergeItemStack(stack, playerInvStart, playerInvStart + 27, false)) {
				return null;
			}
		}

		if (stack.stackSize == 0) {
			slot.putStack(null);
		} else {
			slot.onSlotChanged();
		}
		if (stack.stackSize == copy.stackSize) return null;
		slot.onPickupFromSlot(player, stack);
		return copy;
	}

	/**
	 * Merges into one specific input slot (by container index) if that slot accepts the stack.
	 */
	protected boolean mergeIntoSlot(ItemStack stack, int slotIndex) {
		Slot slot = (Slot) inventorySlots.get(slotIndex);
		return slot.isItemValid(stack) && mergeItemStack(stack, slotIndex, slotIndex + 1, false);
	}
}
