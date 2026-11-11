package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.items.ItemLingeringPotion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.List;

/**
 * Tipped arrow station: up to 8 arrows plus one lingering potion make that many tipped arrows,
 * the same result as the crafting recipe.
 */
public class ContainerFletchingTable extends ContainerWorkbenchBase {

	public static final int ARROWS_PER_POTION = 8;

	public final InventoryCrafting inputs = new InventoryCrafting(this, 2, 1);

	public ContainerFletchingTable(InventoryPlayer inv, World world, int x, int y, int z) {
		super(world, x, y, z);
		addSlotToContainer(new Slot(inputs, 0, 48, 22) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == Items.arrow;
			}
		});
		addSlotToContainer(new Slot(inputs, 1, 48, 48) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return isValidPotion(stack);
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		addResultSlot(124, 35);
		addPlayerInventory(inv);
	}

	public static boolean isValidPotion(ItemStack potion) {
		if (potion == null || potion.getItem() != ModItems.LINGERING_POTION.get()) return false;
		List<PotionEffect> effects = ((ItemLingeringPotion) ModItems.LINGERING_POTION.get()).getEffects(potion);
		return potion.getItemDamage() != 0 || !effects.isEmpty();
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		resultInventory.setInventorySlotContents(0, computeResult());
		super.onCraftMatrixChanged(inv);
	}

	private ItemStack computeResult() {
		ItemStack arrows = inputs.getStackInSlot(0);
		ItemStack potion = inputs.getStackInSlot(1);
		if (arrows == null || !isValidPotion(potion)) return null;
		ItemStack result = ModItems.TIPPED_ARROW.newItemStack(Math.min(ARROWS_PER_POTION, arrows.stackSize), potion.getItemDamage());
		if (potion.hasTagCompound() && potion.getTagCompound().hasKey("CustomPotionEffects", 9)) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag("CustomPotionEffects", potion.getTagCompound().getTagList("CustomPotionEffects", 10).copy());
			result.setTagCompound(tag);
		}
		return result;
	}

	@Override
	protected IInventory getInputInventory() {
		return inputs;
	}

	@Override
	protected void onTakeResult(EntityPlayer player, ItemStack result) {
		ItemStack arrows = inputs.getStackInSlot(0);
		// The result was always computed from this many arrows (the passed stack may already be merged away)
		inputs.decrStackSize(0, arrows == null ? 0 : Math.min(ARROWS_PER_POTION, arrows.stackSize));
		inputs.decrStackSize(1, 1);
		playSound("random.bow");
	}

	@Override
	protected boolean mergeIntoInputs(ItemStack stack) {
		return mergeIntoSlot(stack, 0) || mergeIntoSlot(stack, 1);
	}
}
