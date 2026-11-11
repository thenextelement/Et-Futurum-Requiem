package ganymedes01.etfuturum.core.handlers;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockLightningRod;
import ganymedes01.etfuturum.tileentities.TileEntityLightningRod;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Redirects natural lightning to the nearest lightning rod, like 1.17+.
 */
public final class LightningRodHandler {

	private static final int RANGE = 128;

	private LightningRodHandler() {
	}

	/**
	 * Called from the WorldServer mixin instead of {@code new EntityLightningBolt(world, x, y, z)} for natural strikes.
	 */
	public static EntityLightningBolt createBolt(World world, double x, double y, double z) {
		if (ModBlocks.LIGHTNING_ROD.isEnabled()) {
			TileEntityLightningRod rod = findRod(world, (int) x, (int) z);
			if (rod != null) {
				EntityLightningBolt bolt = new EntityLightningBolt(world, rod.xCoord + 0.5D, rod.yCoord + 1, rod.zCoord + 0.5D);
				((BlockLightningRod) ModBlocks.LIGHTNING_ROD.get()).onLightningStrike(world, rod.xCoord, rod.yCoord, rod.zCoord);
				return bolt;
			}
		}
		return new EntityLightningBolt(world, x, y, z);
	}

	private static TileEntityLightningRod findRod(World world, int x, int z) {
		TileEntityLightningRod best = null;
		double bestDist = Double.MAX_VALUE;
		int minCX = (x - RANGE) >> 4, maxCX = (x + RANGE) >> 4;
		int minCZ = (z - RANGE) >> 4, maxCZ = (z + RANGE) >> 4;
		for (int cx = minCX; cx <= maxCX; cx++) {
			for (int cz = minCZ; cz <= maxCZ; cz++) {
				if (!world.getChunkProvider().chunkExists(cx, cz)) continue;
				Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
				for (Object o : chunk.chunkTileEntityMap.values()) {
					if (!(o instanceof TileEntityLightningRod rod) || ((TileEntity) o).isInvalid()) continue;
					int dx = rod.xCoord - x, dz = rod.zCoord - z;
					if (Math.abs(dx) > RANGE || Math.abs(dz) > RANGE) continue;
					// Only rods that are the highest block in their column can be struck
					if (world.getPrecipitationHeight(rod.xCoord, rod.zCoord) != rod.yCoord + 1) continue;
					double dist = dx * dx + dz * dz;
					if (dist < bestDist) {
						bestDist = dist;
						best = rod;
					}
				}
			}
		}
		return best;
	}
}
