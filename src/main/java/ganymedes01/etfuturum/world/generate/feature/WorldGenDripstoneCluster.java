package ganymedes01.etfuturum.world.generate.feature;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/**
 * A simplified 1.17 dripstone cluster: covers the ceiling and floor of a cave with dripstone blocks and grows
 * stalactites and stalagmites from them. Radius is kept at 6 or less so it never leaves the populated chunk area.
 */
public class WorldGenDripstoneCluster extends WorldGenerator {

	/**
	 * Pointed dripstone metadata: + 0 for down, + STATES for up. Then 0 tip, 1 frustum, 2 middle, 3 base, 4 merged tip.
	 */
	private static final int STATES = 5;
	private static final int MAX_GAP = 12;

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!world.isAirBlock(x, y, z)) return false;
		int radius = 2 + rand.nextInt(5);
		boolean placed = false;

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				double dist = Math.sqrt(dx * dx + dz * dz);
				if (dist > radius + 0.5D) continue;
				int cx = x + dx, cz = z + dz;
				int ceiling = findSurface(world, cx, y, cz, 1);
				int floor = findSurface(world, cx, y, cz, -1);
				if (ceiling < 0 || floor < 0) continue;

				double edge = 1.0D - dist / (radius + 1);
				boolean ceilingDripstone = convert(world, cx, ceiling, cz, rand, edge);
				boolean floorDripstone = convert(world, cx, floor, cz, rand, edge);
				placed |= ceilingDripstone || floorDripstone;

				int gap = ceiling - floor - 1;
				if (gap < 1 || rand.nextDouble() > 0.35D * edge + 0.1D) continue;

				int maxLength = Math.max(1, (int) Math.round(edge * 5));
				int down = ceilingDripstone ? 1 + rand.nextInt(maxLength) : 0;
				int up = floorDripstone ? 1 + rand.nextInt(maxLength) : 0;
				if (down + up > gap) {
					// Too long to fit: either merge them into a column or shorten them
					if (rand.nextBoolean() && down > 0 && up > 0 && gap >= 2) {
						down = gap / 2 + gap % 2;
						up = gap / 2;
					} else {
						down = Math.min(down, gap);
						up = Math.min(up, gap - down);
					}
				}
				boolean merged = down > 0 && up > 0 && down + up == gap;
				placeColumn(world, cx, ceiling - 1, cz, -1, down, merged);
				placeColumn(world, cx, floor + 1, cz, 1, up, merged);
			}
		}
		return placed;
	}

	/**
	 * Finds the first stone-like block above (dir 1) or below (dir -1) that faces the cave.
	 */
	private static int findSurface(World world, int x, int y, int z, int dir) {
		for (int i = 0, yy = y; i < MAX_GAP; i++, yy += dir) {
			if (yy <= 1 || yy >= world.getHeight() - 1) return -1;
			Block block = world.getBlock(x, yy, z);
			if (block.isAir(world, x, yy, z)) continue;
			return isBaseStone(block) ? yy : -1;
		}
		return -1;
	}

	private static boolean convert(World world, int x, int y, int z, Random rand, double edge) {
		if (rand.nextDouble() > 0.4D + edge) return false;
		Block dripstone = ModBlocks.DRIPSTONE_BLOCK.get();
		world.setBlock(x, y, z, dripstone, 0, 2);
		// Thicken the layer towards the centre of the cluster
		int extra = rand.nextDouble() < edge ? 1 : 0;
		int dir = world.isAirBlock(x, y - 1, z) ? 1 : -1;
		for (int i = 1; i <= extra; i++) {
			if (isBaseStone(world.getBlock(x, y + dir * i, z))) {
				world.setBlock(x, y + dir * i, z, dripstone, 0, 2);
			}
		}
		return true;
	}

	/**
	 * Places a pointed dripstone column starting at (x, startY, z) going in dir with the correct thickness states.
	 */
	private static void placeColumn(World world, int x, int startY, int z, int dir, int length, boolean mergedTip) {
		if (length <= 0) return;
		Block pointed = ModBlocks.POINTED_DRIPSTONE.get();
		int offset = dir > 0 ? STATES : 0;
		for (int i = 0; i < length; i++) {
			int y = startY + dir * i;
			if (!world.isAirBlock(x, y, z)) return;
			int fromTip = length - 1 - i;
			int state;
			if (fromTip == 0) {
				state = mergedTip ? 4 : 0;
			} else if (fromTip >= 3 && i == 0) {
				state = 3;
			} else {
				state = Math.min(fromTip, 2);
			}
			world.setBlock(x, y, z, pointed, state + offset, 2);
		}
	}

	private static boolean isBaseStone(Block block) {
		return block == Blocks.stone || block == ModBlocks.DRIPSTONE_BLOCK.get() || block == ModBlocks.DEEPSLATE.get()
				|| block == ModBlocks.TUFF.get() || block == ModBlocks.STONE.get();
	}
}
