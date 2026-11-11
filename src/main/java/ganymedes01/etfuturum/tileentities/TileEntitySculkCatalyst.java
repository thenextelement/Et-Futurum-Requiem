package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.Tags;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class TileEntitySculkCatalyst extends TileEntity {

	/**
	 * How far a charge may wander from where the mob died.
	 */
	private static final int SPREAD_RADIUS = 8;
	private static final int BLOOM_TICKS = 8;

	/**
	 * Spreads sculk around the death position, one block per point of experience, and makes the catalyst bloom.
	 */
	public void bloomWithExperience(int experience, double deathX, double deathY, double deathZ) {
		if (worldObj.isRemote || experience <= 0) return;

		int ox = MathHelper.floor_double(deathX);
		int oy = MathHelper.floor_double(deathY);
		int oz = MathHelper.floor_double(deathZ);
		spread(worldObj, worldObj.rand, ox, oy, oz, experience);

		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 3);
		worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(), BLOOM_TICKS);
		worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Tags.MC_ASSET_VER + ":block.sculk_catalyst.bloom", 2.0F, 0.6F + worldObj.rand.nextFloat() * 0.4F);
	}

	private static void spread(World world, Random rand, int ox, int oy, int oz, int charge) {
		// Start from the surface the mob died on
		int cx = ox, cy = oy, cz = oz;
		for (int i = 0; i < 3 && !isSurface(world, cx, cy, cz); i++) cy--;

		int attempts = charge * 8;
		while (charge > 0 && attempts-- > 0) {
			if (isSurface(world, cx, cy, cz) && isReplaceable(world.getBlock(cx, cy, cz))) {
				world.setBlock(cx, cy, cz, ModBlocks.SCULK.get(), 0, 3);
				charge--;
			}
			// Walk the cursor to a random neighbouring surface block, staying near the death position
			for (int tries = 0; tries < 6; tries++) {
				int nx = cx + rand.nextInt(3) - 1;
				int ny = cy + rand.nextInt(3) - 1;
				int nz = cz + rand.nextInt(3) - 1;
				if (Math.abs(nx - ox) > SPREAD_RADIUS || Math.abs(ny - oy) > SPREAD_RADIUS || Math.abs(nz - oz) > SPREAD_RADIUS) continue;
				Block b = world.getBlock(nx, ny, nz);
				if (isSurface(world, nx, ny, nz) && (b == ModBlocks.SCULK.get() || isReplaceable(b))) {
					cx = nx;
					cy = ny;
					cz = nz;
					break;
				}
			}
		}
	}

	/**
	 * A solid block with at least one face open to air.
	 */
	private static boolean isSurface(World world, int x, int y, int z) {
		if (!world.getBlock(x, y, z).isOpaqueCube()) return false;
		return world.isAirBlock(x + 1, y, z) || world.isAirBlock(x - 1, y, z) || world.isAirBlock(x, y + 1, z)
				|| world.isAirBlock(x, y - 1, z) || world.isAirBlock(x, y, z + 1) || world.isAirBlock(x, y, z - 1);
	}

	/**
	 * Mirrors the vanilla sculk_replaceable tag for blocks that exist in 1.7.10 and this mod.
	 */
	private static boolean isReplaceable(Block block) {
		if (block == Blocks.stone || block == Blocks.dirt || block == Blocks.grass || block == Blocks.mycelium
				|| block == Blocks.sand || block == Blocks.gravel || block == Blocks.sandstone || block == Blocks.netherrack
				|| block == Blocks.soul_sand || block == Blocks.clay || block == Blocks.hardened_clay || block == Blocks.stained_hardened_clay
				|| block == Blocks.end_stone) {
			return true;
		}
		return block == ModBlocks.DEEPSLATE.get() || block == ModBlocks.TUFF.get() || block == ModBlocks.CALCITE.get()
				|| block == ModBlocks.DRIPSTONE_BLOCK.get() || block == ModBlocks.SOUL_SOIL.get() || block == ModBlocks.MOSS_BLOCK.get()
				|| block == ModBlocks.MUD.get() || block == ModBlocks.STONE.get() || block == ModBlocks.SMOOTH_BASALT.get();
	}
}
