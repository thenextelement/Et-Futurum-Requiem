package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * Banner pattern items for the loom. Like 1.21.2+, every item-based pattern needs one; they are not used up.
 */
public class ItemBannerPattern extends BaseSubtypesItem {

	public static final EnumBannerPattern[] PATTERNS = {
			EnumBannerPattern.FLOWER, EnumBannerPattern.CREEPER, EnumBannerPattern.SKULL,
			EnumBannerPattern.MOJANG, EnumBannerPattern.BRICKS, EnumBannerPattern.CURLY_BORDER};

	public ItemBannerPattern() {
		super("flower_banner_pattern", "creeper_banner_pattern", "skull_banner_pattern",
				"mojang_banner_pattern", "field_masoned_banner_pattern", "bordure_indented_banner_pattern");
		setNames("banner_pattern");
		setMaxStackSize(1);
		setCreativeTab(EtFuturum.creativeTabItems);
	}

	public static EnumBannerPattern getPattern(ItemStack stack) {
		return PATTERNS[stack.getItemDamage() % PATTERNS.length];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + getNameDomain() + ".banner_pattern";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.etfuturum." + types[stack.getItemDamage() % types.length] + ".desc"));
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return switch (getPattern(stack)) {
			case CREEPER, SKULL -> EnumRarity.uncommon;
			case MOJANG -> EnumRarity.epic;
			default -> EnumRarity.common;
		};
	}
}
