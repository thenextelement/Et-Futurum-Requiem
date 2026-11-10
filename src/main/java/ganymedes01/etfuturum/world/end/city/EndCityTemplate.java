package ganymedes01.etfuturum.world.end.city;

import ganymedes01.etfuturum.core.utils.Logger;
import ganymedes01.etfuturum.core.utils.structurenbt.BlockStateContainer;
import ganymedes01.etfuturum.core.utils.structurenbt.EFRBlockStateConverter;
import ganymedes01.etfuturum.core.utils.structurenbt.NBTStructure;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * One vanilla end city template, with vanilla's rotation maths: rotation r turns the template r * 90 degrees clockwise
 * around its origin, (x, z) -> (-z, x) per step. Block orientations are rotated on the modern block state properties
 * before EFR's converter turns them into 1.7.10 blocks.
 */
public final class EndCityTemplate {

	private static final Map<String, EndCityTemplate> CACHE = new HashMap<>();
	private static final String[] HORIZONTAL = {"north", "east", "south", "west"};
	/**
	 * Modern dye order, which matches the wool meta EFR banners use for their colours.
	 */
	private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
			"light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};

	public final String name;
	public final int sizeX, sizeY, sizeZ;
	/**
	 * Per block: x, y, z, palette state index.
	 */
	final int[][] blocks;
	/**
	 * Data marker string per block index, or null.
	 */
	final String[] markers;
	/**
	 * Tile entity data to apply after placing, per block index, or null. Only banners: modern item NBT in other
	 * containers (the ship's brewing stands) can't be read by 1.7.10.
	 */
	final NBTTagCompound[] tileData;
	@SuppressWarnings("unchecked")
	private final Map<Integer, BlockStateContainer>[] palettes = new Map[4];

	private EndCityTemplate(String name) {
		this.name = name;
		NBTTagCompound compound;
		String path = "/data/structure/end_city/" + name + ".nbt";
		try (InputStream in = EndCityTemplate.class.getResourceAsStream(path)) {
			compound = CompressedStreamTools.readCompressed(in);
		} catch (Exception e) {
			Logger.error("Failed to read end city template " + path);
			throw new RuntimeException(e);
		}
		NBTTagList size = compound.getTagList("size", 3);
		sizeX = NBTStructure.getPosFromTagList(size).getX();
		sizeY = NBTStructure.getPosFromTagList(size).getY();
		sizeZ = NBTStructure.getPosFromTagList(size).getZ();

		NBTTagList list = compound.getTagList("blocks", 10);
		blocks = new int[list.tagCount()][];
		markers = new String[list.tagCount()];
		tileData = new NBTTagCompound[list.tagCount()];
		NBTTagList palette = compound.getTagList("palette", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound b = list.getCompoundTagAt(i);
			ganymedes01.etfuturum.core.utils.helpers.BlockPos pos = NBTStructure.getPosFromTagList(b.getTagList("pos", 3));
			blocks[i] = new int[]{pos.getX(), pos.getY(), pos.getZ(), b.getInteger("state")};
			if (b.hasKey("nbt", 10) && b.getCompoundTag("nbt").hasKey("metadata")) {
				markers[i] = b.getCompoundTag("nbt").getString("metadata");
			}
			String blockName = palette.getCompoundTagAt(blocks[i][3]).getString("Name");
			if (blockName.endsWith("_banner")) {
				tileData[i] = bannerData(blockName, b.getCompoundTag("nbt"));
			}
		}

		for (int r = 0; r < 4; r++) {
			Set<Pair<Integer, NBTTagCompound>> rotated = new HashSet<>();
			for (int i = 0; i < palette.tagCount(); i++) {
				rotated.add(new ImmutablePair<>(i, rotateState((NBTTagCompound) palette.getCompoundTagAt(i).copy(), r)));
			}
			palettes[r] = EFRBlockStateConverter.INSTANCE.processPalette(ForgeDirection.NORTH, rotated);
		}
	}

	/**
	 * Modern banners keep their base colour in the block name, 1.7.10 ones in the tile entity's "Base".
	 */
	private static NBTTagCompound bannerData(String blockName, NBTTagCompound nbt) {
		String color = blockName.substring(blockName.indexOf(':') + 1).replace("_wall_banner", "").replace("_banner", "");
		NBTTagCompound tag = new NBTTagCompound();
		for (int c = 0; c < COLORS.length; c++) {
			if (COLORS[c].equals(color)) tag.setInteger("Base", c);
		}
		tag.setBoolean("IsStanding", !blockName.contains("wall_banner"));
		if (nbt.hasKey("Patterns", 9)) tag.setTag("Patterns", nbt.getTagList("Patterns", 10).copy());
		return tag;
	}

	public static synchronized EndCityTemplate get(String name) {
		return CACHE.computeIfAbsent(name, EndCityTemplate::new);
	}

	BlockStateContainer getState(int rotation, int stateIndex) {
		return palettes[rotation].get(stateIndex);
	}

	private static NBTTagCompound rotateState(NBTTagCompound state, int r) {
		if (r == 0 || !state.hasKey("Properties", 10)) return state;
		NBTTagCompound props = state.getCompoundTag("Properties");
		if (props.hasKey("facing")) {
			props.setString("facing", rotateHorizontal(props.getString("facing"), r));
		}
		if (props.hasKey("axis") && r % 2 == 1) {
			String axis = props.getString("axis");
			props.setString("axis", axis.equals("x") ? "z" : axis.equals("z") ? "x" : axis);
		}
		if (props.hasKey("rotation")) {
			props.setString("rotation", String.valueOf((Integer.parseInt(props.getString("rotation")) + 4 * r) % 16));
		}
		return state;
	}

	static String rotateHorizontal(String dir, int r) {
		for (int i = 0; i < 4; i++) {
			if (HORIZONTAL[i].equals(dir)) return HORIZONTAL[(i + r) % 4];
		}
		return dir;
	}

	/**
	 * Rotates a template-relative offset clockwise r quarter turns: (x, z) -> (-z, x) per step.
	 */
	static int[] rotate(int x, int y, int z, int r) {
		return switch (r & 3) {
			case 1 -> new int[]{-z, y, x};
			case 2 -> new int[]{-x, y, -z};
			case 3 -> new int[]{z, y, -x};
			default -> new int[]{x, y, z};
		};
	}
}
