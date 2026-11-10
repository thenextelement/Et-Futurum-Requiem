package ganymedes01.etfuturum.compat;

import ganymedes01.etfuturum.items.equipment.ItemShield;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import xonin.backhand.api.core.BackhandUtils;

/**
 * Backhand (GTNH fork) offhand support. Only touches Backhand classes after {@link #isLoaded()} is checked.
 */
public final class CompatBackhand {

	private CompatBackhand() {
	}

	public static boolean isLoaded() {
		return ModsList.BACKHAND.isLoaded();
	}

	/**
	 * A shield in the offhand raises before the main hand item is used, like modern Minecraft.
	 */
	public static void postInit() {
		BackhandUtils.addOffhandPriorityItem(ItemShield.class);
	}

	public static ItemStack getOffhandItem(EntityPlayer player) {
		return BackhandUtils.getOffhandItem(player);
	}

	public static void setOffhandItem(EntityPlayer player, ItemStack stack) {
		BackhandUtils.setPlayerOffhandItem(player, stack);
	}
}
