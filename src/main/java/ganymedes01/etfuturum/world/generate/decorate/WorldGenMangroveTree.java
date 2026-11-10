package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

/**
 * Mangrove tree from 1.19: a trunk raised on arching roots, upward branches and a wide leaf canopy.
 */
public class WorldGenMangroveTree extends WorldGenAbstractTree {

	private static final int[][] ROOT_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

	public WorldGenMangroveTree(boolean notify) {
		super(notify);
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int rootHeight = 1 + rand.nextInt(3);
		int trunkHeight = 2 + rand.nextInt(2) + rand.nextInt(5);
		int baseY = y + rootHeight;
		int topY = baseY + trunkHeight;
		if (topY + 4 >= world.getHeight() || !isGround(world.getBlock(x, y - 1, z))) return false;

		for (int i = y; i <= topY; i++) {
			if (!canReplace(world, x, i, z)) return false;
		}

		Block log = ModBlocks.MANGROVE_LOG.get();
		Block leaves = ModBlocks.LEAVES.get();

		// Central root column and arching roots
		for (int i = y; i < baseY; i++) {
			setRoot(world, x, i, z);
		}
		int arms = 3 + rand.nextInt(3);
		for (int a = 0; a < arms; a++) {
			int[] d = ROOT_DIRS[rand.nextInt(ROOT_DIRS.length)];
			int rx = x, ry = baseY - 1, rz = z;
			for (int step = 0; step < 10 && ry > 0; step++) {
				if (step < 3 || rand.nextBoolean()) {
					rx += d[0];
					rz += d[1];
				}
				ry--;
				if (!canReplace(world, rx, ry, rz) && !isGround(world.getBlock(rx, ry, rz))) break;
				if (isGround(world.getBlock(rx, ry, rz))) {
					if (world.getBlock(rx, ry, rz) == ModBlocks.MUD.get()) {
						setBlockAndNotifyAdequately(world, rx, ry, rz, ModBlocks.MUDDY_MANGROVE_ROOTS.get(), 0);
					}
					break;
				}
				setRoot(world, rx, ry, rz);
			}
		}

		// Trunk
		for (int i = baseY; i <= topY; i++) {
			setBlockAndNotifyAdequately(world, x, i, z, log, 0);
		}

		// Upward branches
		int branches = rand.nextInt(3);
		for (int b = 0; b < branches; b++) {
			int[] d = ROOT_DIRS[rand.nextInt(4)];
			int bx = x, by = topY - 1 - rand.nextInt(2), bz = z;
			int length = 2 + rand.nextInt(2);
			for (int i = 0; i < length; i++) {
				bx += d[0];
				bz += d[1];
				by++;
				if (!canReplace(world, bx, by, bz)) break;
				setBlockAndNotifyAdequately(world, bx, by, bz, log, 0);
			}
			canopy(world, rand, bx, by, bz, leaves);
		}
		canopy(world, rand, x, topY, z, leaves);
		return true;
	}

	private void canopy(World world, Random rand, int cx, int cy, int cz, Block leaves) {
		for (int i = 0; i < 70; i++) {
			int lx = cx + rand.nextInt(4) - rand.nextInt(4);
			int ly = cy + rand.nextInt(3) - rand.nextInt(2);
			int lz = cz + rand.nextInt(4) - rand.nextInt(4);
			if (world.getBlock(lx, ly, lz).canBeReplacedByLeaves(world, lx, ly, lz)) {
				setBlockAndNotifyAdequately(world, lx, ly, lz, leaves, 0);
			}
		}
	}

	private void setRoot(World world, int x, int y, int z) {
		setBlockAndNotifyAdequately(world, x, y, z, ModBlocks.MANGROVE_ROOTS.get(), 0);
	}

	private static boolean isGround(Block block) {
		Material m = block.getMaterial();
		return m == Material.ground || m == Material.grass || m == Material.clay || m == Material.sand || block == ModBlocks.MUD.get();
	}

	private static boolean canReplace(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z) || block.getMaterial() == Material.water
				|| block == ModBlocks.SAPLING.get() || block == ModBlocks.MANGROVE_ROOTS.get() || block.isReplaceable(world, x, y, z);
	}
}
