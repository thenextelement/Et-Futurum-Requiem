package ganymedes01.etfuturum.world.end.dimension;

import ganymedes01.etfuturum.world.DimensionOverrideHelper;
import net.minecraft.world.WorldProviderEnd;

public class DimensionProviderEFREnd {

	/**
	 * True once our provider is actually registered for the End. False when e.g. Hardcore Ender Expansion owns the End.
	 */
	public static boolean active;

	public static void init() {
		active = DimensionOverrideHelper.overrideIfVanilla(1, WorldProviderEnd.class, WorldProviderEFREnd.class, "the Et Futurum End dimension provider");
	}
}
