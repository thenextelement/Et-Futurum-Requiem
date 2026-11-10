package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.recipes.StonecutterRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ContainerStonecutter extends ContainerWorkbenchBase {

	public final InventoryCrafting input = new InventoryCrafting(this, 1, 1);
	private List<ItemStack> recipes = Collections.emptyList();
	private int selectedRecipe = -1;
	private ItemStack lastInput;

	public ContainerStonecutter(InventoryPlayer inv, World world, int x, int y, int z) {
		super(world, x, y, z);
		addSlotToContainer(new Slot(input, 0, 20, 33) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return StonecutterRecipes.hasRecipes(stack);
			}
		});
		addResultSlot(143, 33);
		addPlayerInventory(inv);
	}

	public List<ItemStack> getRecipes() {
		return recipes;
	}

	public int getSelectedRecipe() {
		return selectedRecipe;
	}

	public boolean hasInput() {
		return input.getStackInSlot(0) != null && !recipes.isEmpty();
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		ItemStack in = input.getStackInSlot(0);
		if (in == null || lastInput == null || in.getItem() != lastInput.getItem() || in.getItemDamage() != lastInput.getItemDamage()) {
			recipes = StonecutterRecipes.getOutputs(in);
			selectedRecipe = -1;
		}
		lastInput = in == null ? null : in.copy();
		updateResult();
		super.onCraftMatrixChanged(inv);
	}

	private void updateResult() {
		boolean valid = input.getStackInSlot(0) != null && selectedRecipe >= 0 && selectedRecipe < recipes.size();
		resultInventory.setInventorySlotContents(0, valid ? recipes.get(selectedRecipe).copy() : null);
	}

	/**
	 * Recipe button clicks arrive here on both sides (the GUI calls it locally and sends an enchant packet).
	 */
	@Override
	public boolean enchantItem(EntityPlayer player, int id) {
		if (id >= 0 && id < recipes.size()) {
			selectedRecipe = id;
			updateResult();
			return true;
		}
		return false;
	}

	@Override
	protected IInventory getInputInventory() {
		return input;
	}

	@Override
	protected void onTakeResult(EntityPlayer player, ItemStack result) {
		input.decrStackSize(0, 1);
		playSound(Tags.MC_ASSET_VER + ":ui.stonecutter.take_result");
	}

	@Override
	protected boolean mergeIntoInputs(ItemStack stack) {
		return mergeIntoSlot(stack, 0);
	}
}
