package ganymedes01.etfuturum.core.utils;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

/**
 * Map zooming, cloning and locking for the cartography table. Locked maps are fresh copies under a new map ID that
 * carry a tag on the item; a mixin on ItemMap stops those from being redrawn.
 */
public final class MapLocking {

	public static final String LOCKED = "etfuturum:locked";
	public static final String PENDING_LOCK = "etfuturum:locking";
	public static final String PENDING_ZOOM = "etfuturum:zooming";
	public static final int MAX_SCALE = 4;

	private MapLocking() {
	}

	public static boolean isLocked(ItemStack stack) {
		return stack != null && stack.hasTagCompound() && stack.getTagCompound().getBoolean(LOCKED);
	}

	/**
	 * Applies a pending zoom or lock (server side only). Safe to call more than once.
	 */
	public static void finalizeMap(ItemStack stack, World world) {
		if (world.isRemote || stack == null || !stack.hasTagCompound()) return;
		boolean zoom = stack.getTagCompound().getBoolean(PENDING_ZOOM);
		boolean lock = stack.getTagCompound().getBoolean(PENDING_LOCK);
		if (!zoom && !lock) return;
		stack.getTagCompound().removeTag(PENDING_ZOOM);
		stack.getTagCompound().removeTag(PENDING_LOCK);

		MapData old = Items.filled_map.getMapData(stack, world);
		int id = world.getUniqueDataId("map");
		MapData copy = new MapData("map_" + id);
		copy.dimension = old.dimension;
		copy.xCenter = old.xCenter;
		copy.zCenter = old.zCenter;
		if (zoom) {
			// Same as vanilla's map extending: a blank map one scale level out around the same centre
			copy.scale = (byte) Math.min(old.scale + 1, MAX_SCALE);
		} else {
			copy.scale = old.scale;
			copy.colors = old.colors.clone();
			stack.getTagCompound().setBoolean(LOCKED, true);
		}
		copy.markDirty();
		world.setItemData(copy.mapName, copy);
		stack.setItemDamage(id);
		if (stack.getTagCompound().hasNoTags()) stack.setTagCompound(null);
	}
}
