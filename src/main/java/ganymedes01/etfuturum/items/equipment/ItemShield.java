package ganymedes01.etfuturum.items.equipment;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.lib.EnumColor;
import ganymedes01.etfuturum.recipes.ModRecipes;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

/**
 * 1.9+ shield. Blocking itself is handled by {@link ganymedes01.etfuturum.core.handlers.ShieldHandler}.
 * Banner designs live in "BlockEntityTag" like vanilla, with "Base" in EFR's banner colour order.
 */
public class ItemShield extends Item {

	public static final int DURABILITY = 336;
	/**
	 * Ticks the shield has to be raised before it blocks, like vanilla.
	 */
	public static final int RAISE_TICKS = 5;
	public static final String DISABLED_TAG = "etfuturum:disabled_until";

	public ItemShield() {
		setMaxStackSize(1);
		setMaxDamage(DURABILITY);
		setFull3D();
		setTextureName("planks_oak"); // Only used for break particles; the item renders as a model
		setUnlocalizedName(Utils.getUnlocalisedName("shield"));
		setCreativeTab(EtFuturum.creativeTabItems);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.block;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!isDisabled(stack, world)) {
			player.setItemInUse(stack, getMaxItemUseDuration(stack));
		}
		return stack;
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		for (int id : OreDictionary.getOreIDs(repair)) {
			if (OreDictionary.getOreName(id).equals("plankWood")) return true;
		}
		return false;
	}

	public static boolean isDisabled(ItemStack stack, World world) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(DISABLED_TAG)) return false;
		if (stack.getTagCompound().getLong(DISABLED_TAG) > world.getTotalWorldTime()) return true;
		stack.getTagCompound().removeTag(DISABLED_TAG);
		if (stack.getTagCompound().hasNoTags()) stack.setTagCompound(null);
		return false;
	}

	public static void disable(ItemStack stack, World world, int ticks) {
		if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setLong(DISABLED_TAG, world.getTotalWorldTime() + ticks);
	}

	public static NBTTagCompound getBannerTag(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag", 10) ? stack.getTagCompound().getCompoundTag("BlockEntityTag") : null;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		NBTTagCompound tag = getBannerTag(stack);
		if (tag != null) {
			String key = "item.etfuturum.shield." + ModRecipes.dye_names[EnumColor.fromDamage(tag.getInteger("Base")).getDamage()] + ".name";
			if (StatCollector.canTranslate(key)) return StatCollector.translateToLocal(key);
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
		NBTTagCompound tag = getBannerTag(stack);
		if (tag == null || !tag.hasKey("Patterns")) return;
		NBTTagList patterns = tag.getTagList("Patterns", 10);
		for (int i = 0; i < patterns.tagCount() && i < 6; i++) {
			NBTTagCompound p = patterns.getCompoundTagAt(i);
			EnumBannerPattern pattern = EnumBannerPattern.getPatternByID(p.getString("Pattern"));
			if (pattern != null) {
				EnumColor color = EnumColor.fromDamage(p.getInteger("Color"));
				tooltip.add(StatCollector.translateToLocal("item.banner." + pattern.getPatternName() + "." + ModRecipes.dye_names[color.getDamage()]));
			}
		}
	}
}
