package ganymedes01.etfuturum.command;

import ganymedes01.etfuturum.world.end.city.EndCityGenerator;
import ganymedes01.etfuturum.world.end.gen.WorldGenEndGateway;
import ganymedes01.etfuturum.world.end.gen.WorldGenEndIsland;
import ganymedes01.etfuturum.world.generate.decorate.WorldGenAzaleaTree;
import ganymedes01.etfuturum.world.generate.decorate.WorldGenCherryTrees;
import ganymedes01.etfuturum.world.generate.decorate.WorldGenMangroveTree;
import ganymedes01.etfuturum.world.generate.feature.WorldGenDripstoneCluster;
import ganymedes01.etfuturum.world.generate.feature.WorldGenFossil;
import ganymedes01.etfuturum.world.generate.feature.WorldGenNetherFossil;
import ganymedes01.etfuturum.world.structure.OceanMonument;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * /efrbuild <structure> [rotation] [x y z]: places one of Et Futurum's structures where the player is looking (or at
 * the given position), like Astral Sorcery's build command. Rotation 0-3 is quarter turns clockwise, where it applies.
 */
public class CommandBuild extends CommandBase {

	private interface Builder {
		void build(World world, int x, int y, int z, int rotation, Random rand);
	}

	private static final Map<String, Builder> STRUCTURES = new LinkedHashMap<>();

	static {
		STRUCTURES.put("end_city", EndCityGenerator::buildCity);
		for (String piece : EndCityGenerator.PIECE_NAMES) {
			STRUCTURES.put("end_city/" + piece, (w, x, y, z, r, rand) -> EndCityGenerator.buildPiece(w, piece, x, y, z, r, rand));
		}
		STRUCTURES.put("end_gateway", feature(new WorldGenEndGateway()));
		STRUCTURES.put("end_island", feature(new WorldGenEndIsland()));
		STRUCTURES.put("ocean_monument", (w, x, y, z, r, rand) -> {
			loadAround(w, x + 29, z + 29, 4);
			OceanMonument.buildTemple(w, x, y, z);
		});
		STRUCTURES.put("fossil", feature(new WorldGenFossil()));
		STRUCTURES.put("nether_fossil", feature(new WorldGenNetherFossil()));
		STRUCTURES.put("dripstone_cluster", feature(new WorldGenDripstoneCluster()));
		STRUCTURES.put("azalea_tree", feature(new WorldGenAzaleaTree(true)));
		STRUCTURES.put("cherry_tree", feature(new WorldGenCherryTrees(true)));
		STRUCTURES.put("mangrove_tree", feature(new WorldGenMangroveTree(true)));
	}

	private static Builder feature(WorldGenerator gen) {
		return (w, x, y, z, r, rand) -> {
			loadAround(w, x, z, 3);
			gen.generate(w, rand, x, y, z);
		};
	}

	/**
	 * Unloaded chunks would swallow the blocks, so load the area first.
	 */
	private static void loadAround(World world, int x, int z, int radius) {
		for (int cx = (x >> 4) - radius; cx <= (x >> 4) + radius; cx++) {
			for (int cz = (z >> 4) - radius; cz <= (z >> 4) + radius; cz++) {
				world.getChunkProvider().loadChunk(cx, cz);
			}
		}
	}

	@Override
	public String getCommandName() {
		return "efrbuild";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.etfuturum.build.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1 || !STRUCTURES.containsKey(args[0])) {
			throw new WrongUsageException("commands.etfuturum.build.usage");
		}
		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		int rotation = args.length > 1 ? parseIntBounded(sender, args[1], 0, 3) : MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int x, y, z;
		if (args.length >= 5) {
			x = MathHelper.floor_double(func_110666_a(sender, player.posX, args[2]));
			y = MathHelper.floor_double(func_110666_a(sender, player.posY, args[3]));
			z = MathHelper.floor_double(func_110666_a(sender, player.posZ, args[4]));
		} else {
			int[] target = lookTarget(player);
			x = target[0];
			y = target[1];
			z = target[2];
		}
		STRUCTURES.get(args[0]).build(player.worldObj, x, y, z, rotation, new Random());
		sender.addChatMessage(new ChatComponentTranslation("commands.etfuturum.build.success", args[0], x, y, z));
	}

	/**
	 * The block above the one the player looks at, within 64 blocks, else their feet.
	 */
	private static int[] lookTarget(EntityPlayerMP player) {
		Vec3 eye = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 look = player.getLook(1.0F);
		Vec3 end = eye.addVector(look.xCoord * 64, look.yCoord * 64, look.zCoord * 64);
		MovingObjectPosition hit = player.worldObj.rayTraceBlocks(eye, end);
		if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			return new int[]{hit.blockX, hit.blockY + 1, hit.blockZ};
		}
		return new int[]{MathHelper.floor_double(player.posX), MathHelper.floor_double(player.boundingBox.minY), MathHelper.floor_double(player.posZ)};
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) return getListOfStringsMatchingLastWord(args, STRUCTURES.keySet().toArray(new String[0]));
		if (args.length == 2) return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3");
		return new ArrayList<>();
	}
}
