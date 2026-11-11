package ganymedes01.etfuturum.configuration.configs;

import ganymedes01.etfuturum.configuration.ConfigBase;

import java.io.File;

/**
 * All former experimental features are finished and their options moved: block toggles live in blocksitems.cfg
 * and the dimension providers in world.cfg. The fields stay here so existing references keep working.
 */
public class ConfigExperiments extends ConfigBase {

	private static final String catExperiments = "experiments";

	public static boolean enableSculk;
	public static boolean enableBubbleColumns;
	public static boolean enableCrimsonBlocks;
	public static boolean enableWarpedBlocks;
	public static boolean enableMangroveBlocks;
	public static boolean enableMossAzalea;
	public static boolean enableDripstone;
	public static boolean enableLightningRod;

	public static boolean netherDimensionProvider;
	public static boolean endDimensionProvider;

	public ConfigExperiments(File file) {
		super(file);
		setCategoryComment(catExperiments,
				"""
						There are currently no experimental features.
						Options that used to be here were moved: block toggles are in blocksitems.cfg, the Nether and End dimension providers in world.cfg.
						""");

		configCats.add(getCategory(catExperiments));
	}

	@Override
	protected void syncConfigOptions() {
	}
}
