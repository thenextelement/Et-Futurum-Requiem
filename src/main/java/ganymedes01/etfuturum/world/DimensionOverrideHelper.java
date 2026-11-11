package ganymedes01.etfuturum.world;

import cpw.mods.fml.relauncher.ReflectionHelper;
import ganymedes01.etfuturum.core.utils.Logger;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import java.util.Hashtable;

/**
 * Replaces a dimension's provider only when no other mod (Netherlicious, BoP, HEE...) has already replaced the vanilla one.
 */
public final class DimensionOverrideHelper {

	private DimensionOverrideHelper() {
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends WorldProvider> getRegisteredProvider(int providerType) {
		Hashtable<Integer, Class<? extends WorldProvider>> providers =
				ReflectionHelper.getPrivateValue(DimensionManager.class, null, "providers");
		return providers.get(providerType);
	}

	/**
	 * @return true if the override was applied
	 */
	public static boolean overrideIfVanilla(int providerType, Class<? extends WorldProvider> vanilla, Class<? extends WorldProvider> replacement, String what) {
		Class<? extends WorldProvider> current = getRegisteredProvider(providerType);
		if (current != null && current != vanilla) {
			Logger.info("Not enabling " + what + ": provider for dimension " + providerType + " was already replaced by " + current.getName());
			return false;
		}
		DimensionManager.unregisterProviderType(providerType);
		DimensionManager.registerProviderType(providerType, replacement, true);
		return true;
	}
}
