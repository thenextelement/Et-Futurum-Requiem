package ganymedes01.etfuturum.world.nether.dimension;

import ganymedes01.etfuturum.world.DimensionOverrideHelper;
import net.minecraft.world.WorldProviderHell;

public class DimensionProviderEFRNether {

	/**
	 * True once our provider is actually registered for the Nether.
	 */
	public static boolean active;

	public static void init() {
		active = DimensionOverrideHelper.overrideIfVanilla(-1, WorldProviderHell.class, WorldProviderEFRNether.class, "the Et Futurum Nether dimension provider");
	}
}
