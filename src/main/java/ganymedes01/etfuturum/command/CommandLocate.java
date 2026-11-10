package ganymedes01.etfuturum.command;

import ganymedes01.etfuturum.world.end.city.EndCityGenerator;
import ganymedes01.etfuturum.world.structure.OceanMonument;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Backport of modern /locate: "/locate structure <id>" and "/locate biome <id>". Structure positions come from each
 * generator's own placement rule and the world seed, so nothing is generated; End cities, which depend on terrain,
 * are looked up among the ones already generated.
 */
public class CommandLocate extends CommandBase {

	private static final String[] STRUCTURES = {"village", "stronghold", "mineshaft", "desert_pyramid", "jungle_pyramid",
			"swamp_hut", "fortress", "monument", "end_city"};
	private static final int STRUCTURE_CHUNK_RADIUS = 100;
	private static final int BIOME_RADIUS = 6400, BIOME_STEP = 32;

	@Override
	public String getCommandName() {
		return "locate";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.etfuturum.locate.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2) throw new WrongUsageException("commands.etfuturum.locate.usage");
		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		World world = player.worldObj;
		int px = MathHelper.floor_double(player.posX), pz = MathHelper.floor_double(player.posZ);
		String id = strip(args[1]);
		ChunkPosition found;
		if (args[0].equals("structure")) {
			found = locateStructure(world, id, px, pz);
		} else if (args[0].equals("biome")) {
			BiomeGenBase biome = biomeById(id);
			if (biome == null) throw new CommandException("commands.etfuturum.locate.biome.invalid", args[1]);
			found = locateBiome(world, biome, px, pz);
		} else {
			throw new WrongUsageException("commands.etfuturum.locate.usage");
		}
		if (found == null) {
			throw new CommandException("commands.etfuturum.locate." + args[0] + ".not_found", args[1]);
		}
		int distance = MathHelper.floor_double(Math.sqrt((double) (found.chunkPosX - px) * (found.chunkPosX - px) + (double) (found.chunkPosZ - pz) * (found.chunkPosZ - pz)));
		ChatComponentText coords = new ChatComponentText("[" + found.chunkPosX + ", ~, " + found.chunkPosZ + "]");
		coords.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
				.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + found.chunkPosX + " ~ " + found.chunkPosZ)));
		sender.addChatMessage(new ChatComponentTranslation("commands.etfuturum.locate.success", args[1], coords, distance));
	}

	private static String strip(String id) {
		id = id.toLowerCase(Locale.ROOT);
		return id.startsWith("minecraft:") ? id.substring(10) : id;
	}

	// ---- structures ----

	private interface ChunkCheck {
		boolean at(int cx, int cz);
	}

	private static ChunkPosition locateStructure(World world, String id, int px, int pz) {
		boolean overworld = world.provider.dimensionId == 0 && world.getWorldInfo().isMapFeaturesEnabled();
		switch (id) {
			case "village" -> {
				if (!overworld) return null;
				Village v = new Village(world);
				return spiral(px, pz, v::at);
			}
			case "desert_pyramid", "jungle_pyramid", "swamp_hut" -> {
				if (!overworld) return null;
				Scattered s = new Scattered(world);
				return spiral(px, pz, (cx, cz) -> s.at(cx, cz) && id.equals(scatteredType(world, cx, cz)));
			}
			case "mineshaft" -> {
				if (!overworld) return null;
				Mineshaft m = new Mineshaft(world);
				return spiral(px, pz, m::at);
			}
			case "stronghold" -> {
				if (!overworld) return null;
				return new Stronghold(world).nearest(px, pz);
			}
			case "fortress" -> {
				if (world.provider.dimensionId != -1) return null;
				Fortress f = new Fortress(world);
				return spiral(px, pz, f::at);
			}
			case "monument" -> {
				if (world.provider.dimensionId != 0) return null;
				return spiral(px, pz, (cx, cz) -> OceanMonument.canSpawnAt(world, cx, cz));
			}
			case "end_city" -> {
				if (world.provider.dimensionId != 1) return null;
				return EndCityGenerator.nearestGeneratedCity(world, px, pz);
			}
			default -> throw new CommandException("commands.etfuturum.locate.structure.invalid", id);
		}
	}

	private static String scatteredType(World world, int cx, int cz) {
		BiomeGenBase b = world.getWorldChunkManager().getBiomeGenAt(cx * 16 + 8, cz * 16 + 8);
		if (b == BiomeGenBase.jungle || b == BiomeGenBase.jungleHills) return "jungle_pyramid";
		if (b == BiomeGenBase.swampland) return "swamp_hut";
		return "desert_pyramid";
	}

	/**
	 * Checks chunks in rings around the player and returns the nearest hit of the first ring that has one.
	 */
	private static ChunkPosition spiral(int px, int pz, ChunkCheck check) {
		int ox = px >> 4, oz = pz >> 4;
		for (int r = 0; r <= STRUCTURE_CHUNK_RADIUS; r++) {
			ChunkPosition best = null;
			double bestDist = Double.MAX_VALUE;
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
					int cx = ox + dx, cz = oz + dz;
					if (!check.at(cx, cz)) continue;
					int x = (cx << 4) + 8, z = (cz << 4) + 8;
					double d = (double) (x - px) * (x - px) + (double) (z - pz) * (z - pz);
					if (d < bestDist) {
						bestDist = d;
						best = new ChunkPosition(x, 64, z);
					}
				}
			}
			if (best != null) return best;
		}
		return null;
	}

	/**
	 * Vanilla's generators, used only for their placement checks.
	 */
	private static final class Village extends MapGenVillage {
		Village(World world) {
			worldObj = world;
		}

		boolean at(int cx, int cz) {
			return canSpawnStructureAtCoords(cx, cz);
		}
	}

	private static final class Scattered extends MapGenScatteredFeature {
		Scattered(World world) {
			worldObj = world;
		}

		boolean at(int cx, int cz) {
			return canSpawnStructureAtCoords(cx, cz);
		}
	}

	private static final class Fortress extends MapGenNetherBridge {
		Fortress(World world) {
			worldObj = world;
		}

		boolean at(int cx, int cz) {
			return canSpawnStructureAtCoords(cx, cz);
		}
	}

	private static final class Mineshaft extends MapGenMineshaft {
		private final long a, b, seed;

		Mineshaft(World world) {
			worldObj = world;
			seed = world.getSeed();
			rand.setSeed(seed);
			a = rand.nextLong();
			b = rand.nextLong();
		}

		/**
		 * MapGenBase seeds its random per chunk like this, and MapGenStructure draws once more, before asking.
		 */
		boolean at(int cx, int cz) {
			rand.setSeed((long) cx * a ^ (long) cz * b ^ seed);
			rand.nextInt();
			return canSpawnStructureAtCoords(cx, cz);
		}
	}

	private static final class Stronghold extends MapGenStronghold {
		Stronghold(World world) {
			worldObj = world;
		}

		ChunkPosition nearest(int px, int pz) {
			canSpawnStructureAtCoords(0, 0); // works out all positions on first use
			ChunkPosition best = null;
			double bestDist = Double.MAX_VALUE;
			List<ChunkPosition> list = getCoordList();
			if (list == null) return null;
			for (ChunkPosition p : list) {
				double d = (double) (p.chunkPosX - px) * (p.chunkPosX - px) + (double) (p.chunkPosZ - pz) * (p.chunkPosZ - pz);
				if (d < bestDist) {
					bestDist = d;
					best = p;
				}
			}
			return best;
		}
	}

	// ---- biomes ----

	private static String biomeId(BiomeGenBase biome) {
		return biome.biomeName.toLowerCase(Locale.ROOT).replace(' ', '_').replace("+", "_plus").replace("-", "_");
	}

	private static BiomeGenBase biomeById(String id) {
		for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
			if (b != null && biomeId(b).equals(id)) return b;
		}
		return null;
	}

	private static ChunkPosition locateBiome(World world, BiomeGenBase biome, int px, int pz) {
		for (int r = 0; r <= BIOME_RADIUS; r += BIOME_STEP) {
			for (int dx = -r; dx <= r; dx += BIOME_STEP) {
				for (int dz = -r; dz <= r; dz += BIOME_STEP) {
					if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
					if (world.getWorldChunkManager().getBiomeGenAt(px + dx, pz + dz) == biome) {
						return new ChunkPosition(px + dx, 64, pz + dz);
					}
				}
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) return getListOfStringsMatchingLastWord(args, "structure", "biome");
		if (args.length == 2 && args[0].equals("structure")) return getListOfStringsMatchingLastWord(args, STRUCTURES);
		if (args.length == 2 && args[0].equals("biome")) {
			List<String> ids = new ArrayList<>();
			for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) if (b != null) ids.add(biomeId(b));
			return getListOfStringsMatchingLastWord(args, ids.toArray(new String[0]));
		}
		return new ArrayList<>();
	}
}
