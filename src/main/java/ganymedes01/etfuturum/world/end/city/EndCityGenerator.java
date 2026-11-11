package ganymedes01.etfuturum.world.end.city;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ganymedes01.etfuturum.EtFuturumLootTables;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.core.utils.structurenbt.BlockStateContainer;
import ganymedes01.etfuturum.entities.EntityShulker;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * End city generation for dimension 1 that works whichever mod owns the End (EFR's provider, Hardcore Ender
 * Expansion, ...), because it runs as a regular world generator instead of inside a chunk provider.
 * <p>
 * Cities are laid out when their start chunk populates. Each piece is only ever written into chunk columns that
 * already exist and are populated; the rest is saved and placed when those chunks populate or load, so generation
 * never forces new chunks (no cascading worldgen).
 */
public class EndCityGenerator implements IWorldGenerator {

	public static final EndCityGenerator INSTANCE = new EndCityGenerator();

	private static final int SALT = 10387313;
	/**
	 * Vanilla's 20-chunk grid with one candidate per cell mostly lands in the void. Instead every island chunk is a
	 * candidate with this 1 in N chance, as long as no other city started within MIN_CITY_SPACING chunks.
	 */
	private static final int START_CHANCE = 7;
	private static final int MIN_CITY_SPACING = 11;
	private static final int MIN_HEIGHT = 50;
	/**
	 * Vanilla cities only appear on the outer islands, past the void around the main island.
	 */
	private static final int MIN_CHUNK_DISTANCE = 64;

	private final Deque<long[]> loadQueue = new ArrayDeque<>();

	public static final String[] PIECE_NAMES = {"base_floor", "base_roof", "bridge_end", "bridge_gentle_stairs", "bridge_piece",
			"bridge_steep_stairs", "fat_tower_base", "fat_tower_middle", "fat_tower_top", "second_floor_1", "second_floor_2",
			"second_roof", "ship", "third_floor_1", "third_floor_2", "third_roof", "tower_base", "tower_piece", "tower_top"};

	/**
	 * Builds a whole city at once (for /efrbuild), loading whatever chunks it covers.
	 */
	public static void buildCity(World world, int x, int y, int z, int rotation, Random rand) {
		for (EndCityPieces.Piece piece : EndCityPieces.build(x, y, z, rotation, rand)) place(world, piece, rand);
	}

	/**
	 * End cities depend on the island's terrain, so only already generated ones can be found.
	 */
	public static net.minecraft.world.ChunkPosition nearestGeneratedCity(World world, int px, int pz) {
		net.minecraft.world.ChunkPosition best = null;
		double bestDist = Double.MAX_VALUE;
		for (long k : CityData.get(world).processedStarts) {
			int x = ((int) (k >> 32) << 4) + 8, z = ((int) k << 4) + 8;
			double d = (double) (x - px) * (x - px) + (double) (z - pz) * (z - pz);
			if (d < bestDist) {
				bestDist = d;
				best = new net.minecraft.world.ChunkPosition(x, 64, z);
			}
		}
		return best;
	}

	public static void buildPiece(World world, String name, int x, int y, int z, int rotation, Random rand) {
		place(world, EndCityPieces.Piece.of(name, x, y, z, rotation), rand);
	}

	private static void place(World world, EndCityPieces.Piece piece, Random rand) {
		for (int cx = piece.minX >> 4; cx <= piece.maxX >> 4; cx++) {
			for (int cz = piece.minZ >> 4; cz <= piece.maxZ >> 4; cz++) {
				world.getChunkProvider().loadChunk(cx, cz);
				placePiece(world, piece, cx, cz, rand);
			}
		}
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider generator, IChunkProvider provider) {
		if (world.provider.dimensionId != 1) return;
		CityData data = CityData.get(world);
		placePending(world, data, chunkX, chunkZ);

		long key = key(chunkX, chunkZ);
		if (data.processedStarts.contains(key)) return;
		Random rand = new Random(world.getSeed() ^ (chunkX * 341873128712L + chunkZ * 132897987541L) ^ SALT);
		if (!isCandidate(chunkX, chunkZ, rand) || data.hasCityNear(chunkX, chunkZ, MIN_CITY_SPACING)) return;
		int rotation = rand.nextInt(4);
		int[] start = findStart(world, chunkX, chunkZ, rotation);
		if (start[1] < MIN_HEIGHT) return;
		data.processedStarts.add(key);
		data.markDirty();

		for (EndCityPieces.Piece piece : EndCityPieces.build(start[0], start[1], start[2], rotation, rand)) {
			for (int cx = piece.minX >> 4; cx <= piece.maxX >> 4; cx++) {
				for (int cz = piece.minZ >> 4; cz <= piece.maxZ >> 4; cz++) {
					if ((cx == chunkX && cz == chunkZ) || isReady(world, cx, cz)) {
						placePiece(world, piece, cx, cz, rand);
					} else {
						data.pending.computeIfAbsent(key(cx, cz), k -> new ArrayList<>()).add(piece);
						data.markDirty();
					}
				}
			}
		}
	}

	private static long key(int cx, int cz) {
		return (long) cx << 32 | (cz & 0xFFFFFFFFL);
	}

	private static boolean isCandidate(int chunkX, int chunkZ, Random rand) {
		if ((long) chunkX * chunkX + (long) chunkZ * chunkZ < (long) MIN_CHUNK_DISTANCE * MIN_CHUNK_DISTANCE) return false;
		return rand.nextInt(START_CHANCE) == 0;
	}

	/**
	 * Finds where the city starts in this chunk: {x, y, z}, with y = -1 when there is no room. Vanilla only samples
	 * around the chunk centre, which misses most small islands, so this anchors on the highest column instead. Like
	 * vanilla it then takes the lowest of four points 4 blocks apart, all inside this chunk so nothing else loads.
	 */
	private static int[] findStart(World world, int chunkX, int chunkZ, int rotation) {
		int bx = chunkX << 4, bz = chunkZ << 4;
		int i = rotation == 1 || rotation == 2 ? -4 : 4;
		int j = rotation == 2 || rotation == 3 ? -4 : 4;
		int[] best = {bx + 8, -1, bz + 8};
		for (int x = bx + 4; x <= bx + 11; x++) {
			for (int z = bz + 4; z <= bz + 11; z++) {
				int h = top(world, x, z);
				if (h <= best[1]) continue;
				h = Math.min(h, Math.min(top(world, x + i, z), Math.min(top(world, x, z + j), top(world, x + i, z + j))));
				if (h > best[1]) best = new int[]{x, h, z};
			}
		}
		return best;
	}

	private static int top(World world, int x, int z) {
		for (int y = world.getActualHeight() - 1; y > 0; y--) {
			Block b = world.getBlock(x, y, z);
			if (!b.isAir(world, x, y, z)) return b.getMaterial().isLiquid() ? -1 : y;
		}
		return -1;
	}

	/**
	 * A chunk we can write into without generating anything: loaded and already populated.
	 */
	private static boolean isReady(World world, int cx, int cz) {
		return world.getChunkProvider().chunkExists(cx, cz) && world.getChunkFromChunkCoords(cx, cz).isTerrainPopulated;
	}

	private void placePending(World world, CityData data, int cx, int cz) {
		List<EndCityPieces.Piece> list = data.pending.remove(key(cx, cz));
		if (list == null) return;
		data.markDirty();
		Random rand = new Random(world.getSeed() ^ key(cx, cz));
		for (EndCityPieces.Piece piece : list) placePiece(world, piece, cx, cz, rand);
	}

	/**
	 * Places the part of a piece that falls in one chunk column.
	 */
	private static void placePiece(World world, EndCityPieces.Piece piece, int cx, int cz, Random rand) {
		EndCityTemplate t = EndCityTemplate.get(piece.name);
		List<int[]> markers = new ArrayList<>();
		for (int i = 0; i < t.blocks.length; i++) {
			int[] b = t.blocks[i];
			int[] o = EndCityTemplate.rotate(b[0], b[1], b[2], piece.rotation);
			int x = piece.x + o[0], y = piece.y + o[1], z = piece.z + o[2];
			if (x >> 4 != cx || z >> 4 != cz || y < 0 || y > 255) continue;
			if (t.markers[i] != null) {
				markers.add(new int[]{i, x, y, z});
				continue;
			}
			BlockStateContainer state = t.getState(piece.rotation, b[3]);
			if (state == null || state.getBlock() == null) continue;
			if (!piece.overwrite && state.getBlock() == Blocks.air) continue;
			world.setBlock(x, y, z, state.getBlock(), state.getMeta(), 2);
			if (t.tileData[i] != null) {
				TileEntity te = world.getTileEntity(x, y, z);
				if (te != null) {
					NBTTagCompound nbt = new NBTTagCompound();
					te.writeToNBT(nbt);
					for (Object key : t.tileData[i].func_150296_c()) {
						nbt.setTag((String) key, t.tileData[i].getTag((String) key).copy());
					}
					te.readFromNBT(nbt);
					te.markDirty();
				}
			}
			if (state.getType() == BlockStateContainer.BlockStateType.BLOCK_ENTITY && state.getCompound() != null) {
				TileEntity te = world.getTileEntity(x, y, z);
				if (te != null) {
					NBTTagCompound nbt = (NBTTagCompound) state.getCompound().copy();
					nbt.setInteger("x", x);
					nbt.setInteger("y", y);
					nbt.setInteger("z", z);
					te.readFromNBT(nbt);
					te.markDirty();
				}
			}
			// After the template's own data, which would otherwise empty it again
			if (state.getBlock() == Blocks.brewing_stand || world.getTileEntity(x, y, z) instanceof TileEntityBrewingStand) {
				fillBrewingStand(world, x, y, z, rand);
			}
		}
		for (int[] m : markers) {
			handleMarker(world, t.markers[m[0]], m[1], m[2], m[3], piece.rotation, rand, piece.overwrite);
		}
	}

	/**
	 * Damage values of 1.7.10 potions: Healing II, Regeneration II, Strength II, Swiftness II, and long Fire
	 * Resistance, Night Vision, Water Breathing, Invisibility. Healing is listed twice, as vanilla ships carry it.
	 */
	private static final int[] SHIP_POTIONS = {8229, 8229, 8225, 8233, 8226, 8259, 8262, 8269, 8270};

	/**
	 * The ship's brewing stands hold modern items that 1.7.10 can't read, so fill them with beneficial potions.
	 */
	private static void fillBrewingStand(World world, int x, int y, int z, Random rand) {
		if (world.getTileEntity(x, y, z) instanceof TileEntityBrewingStand stand) {
			for (int slot = 0; slot < 3; slot++) {
				stand.setInventorySlotContents(slot, new ItemStack(Items.potionitem, 1, SHIP_POTIONS[rand.nextInt(SHIP_POTIONS.length)]));
			}
		}
	}

	private static void handleMarker(World world, String marker, int x, int y, int z, int rotation, Random rand, boolean overwrite) {
		if (overwrite) world.setBlock(x, y, z, Blocks.air, 0, 2);
		switch (marker) {
			case "Chest" -> {
				TileEntity te = world.getTileEntity(x, y - 1, z);
				if (te instanceof IInventory inv) {
					WeightedRandomChestContent.generateChestContents(rand, EtFuturumLootTables.END_CITY_TREASURE.getItems(rand), inv, EtFuturumLootTables.END_CITY_TREASURE.getCount(rand));
				}
			}
			case "Sentry" -> {
				EntityShulker shulker = new EntityShulker(world);
				shulker.setPosition(x + 0.5D, y, z + 0.5D);
				shulker.setAttachmentPos(new BlockPos(x, y, z));
				world.spawnEntityInWorld(shulker);
			}
			case "Elytra" -> {
				// Vanilla hangs the frame facing south, rotated with the piece; 1.7.10 directions: 0 S, 1 W, 2 N, 3 E
				int[] facing = EndCityTemplate.rotate(0, 0, 1, rotation);
				int dir = facing[2] == 1 ? 0 : facing[0] == -1 ? 1 : facing[2] == -1 ? 2 : 3;
				EntityItemFrame frame = new EntityItemFrame(world, x - facing[0], y, z - facing[2], dir);
				frame.setDisplayedItem(new ItemStack(ModItems.ELYTRA.isEnabled() ? ModItems.ELYTRA.get() : Items.feather));
				world.spawnEntityInWorld(frame);
			}
			default -> {
			}
		}
	}

	/**
	 * Chunks that were already populated but unloaded when their city was laid out get their parts when they load.
	 */
	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		World world = event.world;
		if (world.isRemote || world.provider.dimensionId != 1) return;
		Chunk chunk = event.getChunk();
		if (chunk.isTerrainPopulated && CityData.get(world).pending.containsKey(key(chunk.xPosition, chunk.zPosition))) {
			loadQueue.add(new long[]{chunk.xPosition, chunk.zPosition});
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.provider.dimensionId != 1) return;
		for (int i = 0; i < 4 && !loadQueue.isEmpty(); i++) {
			long[] c = loadQueue.poll();
			if (event.world.getChunkProvider().chunkExists((int) c[0], (int) c[1])) {
				placePending(event.world, CityData.get(event.world), (int) c[0], (int) c[1]);
			}
		}
	}

	public static class CityData extends WorldSavedData {
		private static final String NAME = "etfuturum_end_cities";
		final Set<Long> processedStarts = new HashSet<>();
		final Map<Long, List<EndCityPieces.Piece>> pending = new HashMap<>();

		public CityData(String name) {
			super(name);
		}

		boolean hasCityNear(int cx, int cz, int radius) {
			for (long k : processedStarts) {
				int dx = (int) (k >> 32) - cx, dz = (int) k - cz;
				if (dx * dx + dz * dz < radius * radius) return true;
			}
			return false;
		}

		static CityData get(World world) {
			CityData data = (CityData) world.perWorldStorage.loadData(CityData.class, NAME);
			if (data == null) {
				data = new CityData(NAME);
				world.perWorldStorage.setData(NAME, data);
			}
			return data;
		}

		@Override
		public void readFromNBT(NBTTagCompound tag) {
			processedStarts.clear();
			pending.clear();
			NBTTagList starts = tag.getTagList("Starts", 10);
			for (int i = 0; i < starts.tagCount(); i++) {
				NBTTagCompound s = starts.getCompoundTagAt(i);
				processedStarts.add(key(s.getInteger("x"), s.getInteger("z")));
			}
			NBTTagList chunks = tag.getTagList("Pending", 10);
			for (int i = 0; i < chunks.tagCount(); i++) {
				NBTTagCompound c = chunks.getCompoundTagAt(i);
				List<EndCityPieces.Piece> list = new ArrayList<>();
				NBTTagList pieces = c.getTagList("Pieces", 10);
				for (int j = 0; j < pieces.tagCount(); j++) list.add(EndCityPieces.Piece.read(pieces.getCompoundTagAt(j)));
				pending.put(key(c.getInteger("x"), c.getInteger("z")), list);
			}
		}

		@Override
		public void writeToNBT(NBTTagCompound tag) {
			NBTTagList starts = new NBTTagList();
			for (long k : processedStarts) {
				NBTTagCompound s = new NBTTagCompound();
				s.setInteger("x", (int) (k >> 32));
				s.setInteger("z", (int) k);
				starts.appendTag(s);
			}
			tag.setTag("Starts", starts);
			NBTTagList chunks = new NBTTagList();
			for (Map.Entry<Long, List<EndCityPieces.Piece>> e : pending.entrySet()) {
				NBTTagCompound c = new NBTTagCompound();
				c.setInteger("x", (int) (e.getKey() >> 32));
				c.setInteger("z", (int) (long) e.getKey());
				NBTTagList pieces = new NBTTagList();
				for (EndCityPieces.Piece p : e.getValue()) pieces.appendTag(p.write());
				c.setTag("Pieces", pieces);
				chunks.appendTag(c);
			}
			tag.setTag("Pending", chunks);
		}
	}
}
