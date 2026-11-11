package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.items.ItemBannerPattern;
import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.blocks.itemblocks.ItemBlockBanner;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.lib.EnumColor;
import ganymedes01.etfuturum.tileentities.TileEntityBanner;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainerLoom extends ContainerWorkbenchBase {

	public static final int MAX_PATTERNS = 6;

	/**
	 * Patterns that need no pattern item.
	 */
	public static final List<EnumBannerPattern> FREE_PATTERNS;

	static {
		List<EnumBannerPattern> list = new ArrayList<>();
		for (EnumBannerPattern p : EnumBannerPattern.VALUES) {
			if (p != EnumBannerPattern.BASE && p.hasValidCrafting() && (!p.hasCraftingStack() || !ModItems.BANNER_PATTERN.isEnabled())) {
				list.add(p);
			}
		}
		FREE_PATTERNS = Collections.unmodifiableList(list);
	}

	public final InventoryCrafting inputs = new InventoryCrafting(this, 3, 1);
	private List<EnumBannerPattern> patterns = Collections.emptyList();
	private int selectedPattern = -1;

	public ContainerLoom(InventoryPlayer inv, World world, int x, int y, int z) {
		super(world, x, y, z);
		addSlotToContainer(new Slot(inputs, 0, 13, 26) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == ModBlocks.BANNER.getItem();
			}
		});
		addSlotToContainer(new Slot(inputs, 1, 33, 26) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return getDyeColor(stack) >= 0;
			}
		});
		addSlotToContainer(new Slot(inputs, 2, 23, 45) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return getItemPattern(stack) != null;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		addResultSlot(143, 58);
		addPlayerInventory(inv);
	}

	/**
	 * The pattern unlocked by a banner pattern item in the pattern slot. Like 1.21.2+, bricks and the curly border need one too.
	 */
	public static EnumBannerPattern getItemPattern(ItemStack stack) {
		if (stack == null || !ModItems.BANNER_PATTERN.isEnabled() || stack.getItem() != ModItems.BANNER_PATTERN.get()) return null;
		return ItemBannerPattern.getPattern(stack);
	}

	public static int getDyeColor(ItemStack stack) {
		if (stack == null) return -1;
		for (String ore : Utils.getOreNames(stack))
			for (EnumColor colour : EnumColor.VALUES)
				if (ore.equals(colour.getOreName()))
					return colour.getDamage();
		return -1;
	}

	public List<EnumBannerPattern> getPatterns() {
		return patterns;
	}

	public int getSelectedPattern() {
		return selectedPattern;
	}

	public boolean hasMaxPatterns() {
		ItemStack banner = inputs.getStackInSlot(0);
		return banner != null && TileEntityBanner.getPatterns(banner) >= MAX_PATTERNS;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		ItemStack banner = inputs.getStackInSlot(0);
		ItemStack dye = inputs.getStackInSlot(1);
		EnumBannerPattern itemPattern = getItemPattern(inputs.getStackInSlot(2));
		List<EnumBannerPattern> newPatterns = banner == null || dye == null ? Collections.emptyList()
				: itemPattern != null ? Collections.singletonList(itemPattern) : FREE_PATTERNS;
		if (!newPatterns.equals(patterns)) {
			patterns = newPatterns;
			selectedPattern = itemPattern != null && !patterns.isEmpty() ? 0 : -1;
		}
		updateResult();
		super.onCraftMatrixChanged(inv);
	}

	private void updateResult() {
		ItemStack banner = inputs.getStackInSlot(0);
		int color = getDyeColor(inputs.getStackInSlot(1));
		if (banner == null || color < 0 || selectedPattern < 0 || selectedPattern >= patterns.size() || hasMaxPatterns()) {
			resultInventory.setInventorySlotContents(0, null);
			return;
		}
		ItemStack result = banner.copy();
		result.stackSize = 1;
		NBTTagCompound tag = ItemBlockBanner.getSubTag(result, "BlockEntityTag", true);
		NBTTagList list = tag.hasKey("Patterns", 9) ? tag.getTagList("Patterns", 10) : new NBTTagList();
		NBTTagCompound pattern = new NBTTagCompound();
		pattern.setString("Pattern", patterns.get(selectedPattern).getPatternID());
		pattern.setInteger("Color", color);
		list.appendTag(pattern);
		tag.setTag("Patterns", list);
		resultInventory.setInventorySlotContents(0, result);
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int id) {
		if (id >= 0 && id < patterns.size()) {
			selectedPattern = id;
			updateResult();
			return true;
		}
		return false;
	}

	@Override
	protected IInventory getInputInventory() {
		return inputs;
	}

	@Override
	protected void onTakeResult(EntityPlayer player, ItemStack result) {
		inputs.decrStackSize(0, 1);
		inputs.decrStackSize(1, 1);
		playSound(Tags.MC_ASSET_VER + ":ui.loom.take_result");
	}

	@Override
	protected boolean mergeIntoInputs(ItemStack stack) {
		return mergeIntoSlot(stack, 0) || mergeIntoSlot(stack, 1) || mergeIntoSlot(stack, 2);
	}
}
