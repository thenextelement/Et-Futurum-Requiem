package ganymedes01.etfuturum.recipes.crafting;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.items.equipment.ItemShield;
import ganymedes01.etfuturum.tileentities.TileEntityBanner;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Undecorated shield + banner: the shield takes the banner's colour and patterns, the banner is used up.
 */
public class RecipeShieldDecoration implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting grid, World world) {
		return getCraftingResult(grid) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting grid) {
		ItemStack shield = null, banner = null;
		Item bannerItem = Item.getItemFromBlock(ModBlocks.BANNER.get());
		for (int i = 0; i < grid.getSizeInventory(); i++) {
			ItemStack slot = grid.getStackInSlot(i);
			if (slot == null) continue;
			if (slot.getItem() instanceof ItemShield) {
				if (shield != null || ItemShield.getBannerTag(slot) != null) return null;
				shield = slot;
			} else if (slot.getItem() == bannerItem) {
				if (banner != null) return null;
				banner = slot;
			} else {
				return null;
			}
		}
		if (shield == null || banner == null) return null;

		ItemStack result = shield.copy();
		result.stackSize = 1;
		NBTTagCompound design = new NBTTagCompound();
		NBTTagCompound bannerTag = banner.hasTagCompound() ? banner.getTagCompound().getCompoundTag("BlockEntityTag") : null;
		if (bannerTag != null && bannerTag.hasKey("Patterns", 9)) {
			design.setTag("Patterns", bannerTag.getTagList("Patterns", 10).copy());
		}
		design.setInteger("Base", TileEntityBanner.getBaseColor(banner));
		if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound());
		result.getTagCompound().setTag("BlockEntityTag", design);
		return result;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}
