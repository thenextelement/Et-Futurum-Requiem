package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/**
 * Azalea tree from 1.17: an oak trunk that may bend near the top, with a random spread of azalea and flowering azalea leaves.
 */
public class WorldGenAzaleaTree extends WorldGenAbstractTree {

	public WorldGenAzaleaTree(boolean notify) {
		super(notify);
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		Block ground = world.getBlock(x, y - 1, z);
		if (ground != Blocks.grass && ground != Blocks.dirt && ground != ModBlocks.MOSS_BLOCK.get() && ground.getMaterial() != Material.clay) {
			return false;
		}

		int straight = 2 + rand.nextInt(2);
		int bendLength = rand.nextBoolean() ? 1 : 0;
		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[2 + rand.nextInt(4)];
		if (y + straight + bendLength + 3 >= world.getHeight()) return false;

		// Check the trunk path is free
		for (int i = 0; i < straight; i++) {
			if (!canReplace(world, x, y + i, z)) return false;
		}
		for (int i = 1; i <= bendLength; i++) {
			if (!canReplace(world, x + dir.offsetX * i, y + straight - 1 + i, z + dir.offsetZ * i)) return false;
		}

		if (ground == Blocks.grass) {
			world.setBlock(x, y - 1, z, Blocks.dirt);
		}
		for (int i = 0; i < straight; i++) {
			setBlockAndNotifyAdequately(world, x, y + i, z, Blocks.log, 0);
		}
		int tx = x, ty = y + straight - 1, tz = z;
		for (int i = 1; i <= bendLength; i++) {
			tx = x + dir.offsetX * i;
			ty = y + straight - 1 + i;
			tz = z + dir.offsetZ * i;
			setBlockAndNotifyAdequately(world, tx, ty, tz, Blocks.log, 0);
		}

		// Random spread foliage (radius 3, height 2, 50 attempts) like vanilla
		Block leaves = ModBlocks.AZALEA_LEAVES.get();
		for (int i = 0; i < 50; i++) {
			int lx = tx + rand.nextInt(4) - rand.nextInt(4);
			int ly = ty + rand.nextInt(2) - rand.nextInt(2) + 1;
			int lz = tz + rand.nextInt(4) - rand.nextInt(4);
			if (world.getBlock(lx, ly, lz).canBeReplacedByLeaves(world, lx, ly, lz)) {
				setBlockAndNotifyAdequately(world, lx, ly, lz, leaves, rand.nextInt(5) == 0 ? 1 : 0);
			}
		}
		return true;
	}

	private static boolean canReplace(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z) || block == ModBlocks.AZALEA.get() || block.isReplaceable(world, x, y, z);
	}
}
