package ganymedes01.etfuturum.compat;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import net.minecraft.block.Block;

/**
 * Decides which prismarine set is in use. Priority: Botania, then EFR, then Village Names.
 * Village Names ranks below EFR, so its prismarine never disables EFR's.
 * Botania has no prismarine config flag, so its prismarine counts as enabled whenever Botania is loaded.
 */
public final class PrismarineCompat {

	private PrismarineCompat() {
	}

	/** True when Botania's prismarine wins, which hides EFR's prismarine blocks and items. */
	public static boolean botaniaPrismarine() {
		return ModsList.BOTANIA.isLoaded();
	}

	/** Used when registering EFR's prismarine blocks and items. */
	public static boolean efrPrismarineEnabled() {
		return ConfigBlocksItems.enablePrismarine && !botaniaPrismarine();
	}

	/** Prismarine block for worldgen: Botania's when it wins, otherwise EFR's. Meta 0/1/2 match in both mods. */
	public static Block prismarineBlock() {
		if (botaniaPrismarine()) {
			Block botania = GameRegistry.findBlock(ModsList.BOTANIA.modID(), "prismarine");
			if (botania != null) return botania;
		}
		return ModBlocks.PRISMARINE_BLOCK.get();
	}

	/** Sea lantern for worldgen: Botania's sea lamp when it wins, otherwise EFR's. */
	public static Block seaLantern() {
		if (botaniaPrismarine()) {
			Block botania = GameRegistry.findBlock(ModsList.BOTANIA.modID(), "seaLamp");
			if (botania != null) return botania;
		}
		return ModBlocks.SEA_LANTERN.get();
	}

	/** Whether ocean monuments can be generated with the available prismarine set. */
	public static boolean monumentBlocksAvailable() {
		return efrPrismarineEnabled() || botaniaPrismarine();
	}
}
