package ganymedes01.etfuturum.world.end.city;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Port of vanilla's (1.12+) EndCityPieces: the recursive house tower / tower / fat tower / bridge / ship layout.
 */
public final class EndCityPieces {

	private static final int MAX_DEPTH = 8;
	/**
	 * SHIP_ROLLS in SHIP_CHANCE cities get an End ship (about 2 in 7).
	 */
	private static final int SHIP_ROLLS = 2, SHIP_CHANCE = 7;
	private static final int[][] TOWER_BRIDGES = {{0, 1, -1, 0}, {1, 6, -1, 1}, {3, 0, -1, 5}, {2, 5, -1, 6}};
	private static final int[][] FAT_TOWER_BRIDGES = {{0, 4, -1, 0}, {1, 12, -1, 4}, {3, 0, -1, 8}, {2, 8, -1, 12}};

	private EndCityPieces() {
	}

	public static final class Piece {
		public final String name;
		public final int x, y, z, rotation;
		public final boolean overwrite;
		final int minX, minY, minZ, maxX, maxY, maxZ;
		int genDepth;

		Piece(String name, int x, int y, int z, int rotation, boolean overwrite) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
			this.rotation = rotation & 3;
			this.overwrite = overwrite;
			EndCityTemplate t = EndCityTemplate.get(name);
			int[] a = EndCityTemplate.rotate(0, 0, 0, this.rotation);
			int[] b = EndCityTemplate.rotate(t.sizeX - 1, t.sizeY - 1, t.sizeZ - 1, this.rotation);
			minX = x + Math.min(a[0], b[0]);
			maxX = x + Math.max(a[0], b[0]);
			minY = y + Math.min(a[1], b[1]);
			maxY = y + Math.max(a[1], b[1]);
			minZ = z + Math.min(a[2], b[2]);
			maxZ = z + Math.max(a[2], b[2]);
		}

		boolean intersects(Piece o) {
			return maxX >= o.minX && minX <= o.maxX && maxZ >= o.minZ && minZ <= o.maxZ && maxY >= o.minY && minY <= o.maxY;
		}

		public boolean overlapsChunk(int cx, int cz) {
			return maxX >= cx << 4 && minX <= (cx << 4) + 15 && maxZ >= cz << 4 && minZ <= (cz << 4) + 15;
		}

		public static Piece of(String name, int x, int y, int z, int rotation) {
			return new Piece(name, x, y, z, rotation, true);
		}

		public NBTTagCompound write() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("n", name);
			tag.setIntArray("p", new int[]{x, y, z, rotation, overwrite ? 1 : 0});
			return tag;
		}

		public static Piece read(NBTTagCompound tag) {
			int[] p = tag.getIntArray("p");
			return new Piece(tag.getString("n"), p[0], p[1], p[2], p[3], p[4] != 0);
		}
	}

	private interface Generator {
		boolean generate(int depth, Piece parent, int[] pos, List<Piece> pieces, Random rand);
	}

	private static final class Builder {
		final List<Piece> pieces = new ArrayList<>();
		final boolean wantShip;
		boolean shipCreated;

		Builder(boolean wantShip) {
			this.wantShip = wantShip;
			// Vanilla rolls 1 in (10 - depth) per bridge; here the city decides up front, so no roll means no ship
			this.shipCreated = !wantShip;
		}

		/**
		 * New piece placed relative to the previous one, like vanilla's addPiece/calculateConnectedPosition.
		 */
		Piece add(List<Piece> list, Piece previous, int ox, int oy, int oz, String name, int rotation, boolean overwrite) {
			int[] off = EndCityTemplate.rotate(ox, oy, oz, previous.rotation);
			Piece p = new Piece(name, previous.x + off[0], previous.y + off[1], previous.z + off[2], rotation, overwrite);
			list.add(p);
			return p;
		}

		/**
		 * Like vanilla, children are only checked against (and added to) the list of the generator that spawned them.
		 */
		boolean recursiveChildren(Generator gen, int depth, Piece parent, int[] pos, List<Piece> pieces, Random rand) {
			if (depth > MAX_DEPTH) return false;
			List<Piece> list = new ArrayList<>();
			if (gen.generate(depth, parent, pos, list, rand)) {
				int i = rand.nextInt();
				for (Piece p : list) {
					p.genDepth = i;
					Piece hit = findIntersecting(pieces, p);
					if (hit != null && hit.genDepth != parent.genDepth) return false;
				}
				pieces.addAll(list);
				return true;
			}
			return false;
		}

		static Piece findIntersecting(List<Piece> pieces, Piece p) {
			for (Piece existing : pieces) {
				if (existing.intersects(p)) return existing;
			}
			return null;
		}

		final Generator houseTower = (depth, parent, pos, list, rand) -> {
			if (depth > MAX_DEPTH) return false;
			int rot = parent.rotation;
			Piece p = add(list, parent, pos[0], pos[1], pos[2], "base_floor", rot, true);
			int i = rand.nextInt(3);
			if (i == 0) {
				add(list, p, -1, 4, -1, "base_roof", rot, true);
			} else if (i == 1) {
				p = add(list, p, -1, 0, -1, "second_floor_2", rot, false);
				p = add(list, p, -1, 8, -1, "second_roof", rot, false);
				recursiveChildren(this.tower, depth + 1, p, null, list, rand);
			} else {
				p = add(list, p, -1, 0, -1, "second_floor_2", rot, false);
				p = add(list, p, -1, 4, -1, "third_floor_2", rot, false);
				p = add(list, p, -1, 8, -1, "third_roof", rot, true);
				recursiveChildren(this.tower, depth + 1, p, null, list, rand);
			}
			return true;
		};

		final Generator towerBridge = (depth, parent, pos, list, rand) -> {
			int rot = parent.rotation;
			int count = rand.nextInt(4) + 1;
			Piece p = add(list, parent, 0, 0, -4, "bridge_piece", rot, true);
			p.genDepth = -1;
			int j = 0;
			for (int k = 0; k < count; k++) {
				if (rand.nextBoolean()) {
					p = add(list, p, 0, j, -4, "bridge_piece", rot, true);
					j = 0;
				} else {
					if (rand.nextBoolean()) {
						p = add(list, p, 0, j, -4, "bridge_steep_stairs", rot, true);
					} else {
						p = add(list, p, 0, j, -8, "bridge_gentle_stairs", rot, true);
					}
					j = 4;
				}
			}
			if (!shipCreated && rand.nextInt(Math.max(1, 3 - depth / 2)) == 0) {
				// 15 blocks closer than vanilla: the deck is 22-30 blocks from the bridge end, well inside an ender pearl's arc
				add(list, p, -8 + rand.nextInt(8), j, -55 + rand.nextInt(10), "ship", rot, true);
				shipCreated = true;
			} else if (!recursiveChildren(this.houseTower, depth + 1, p, new int[]{-3, j + 1, -11}, list, rand)) {
				return false;
			}
			p = add(list, p, 4, j, 0, "bridge_end", rot + 2, true);
			p.genDepth = -1;
			return true;
		};

		final Generator tower = (depth, parent, pos, list, rand) -> {
			int rot = parent.rotation;
			Piece p = add(list, parent, 3 + rand.nextInt(2), -3, 3 + rand.nextInt(2), "tower_base", rot, true);
			p = add(list, p, 0, 7, 0, "tower_piece", rot, true);
			Piece bridgeFrom = rand.nextInt(3) == 0 ? p : null;
			int floors = 1 + rand.nextInt(3);
			for (int j = 0; j < floors; j++) {
				p = add(list, p, 0, 4, 0, "tower_piece", rot, true);
				if (j < floors - 1 && rand.nextBoolean()) bridgeFrom = p;
			}
			if (bridgeFrom != null) {
				for (int[] b : TOWER_BRIDGES) {
					if (rand.nextBoolean()) {
						Piece bridge = add(list, bridgeFrom, b[1], b[2], b[3], "bridge_end", rot + b[0], true);
						recursiveChildren(this.towerBridge, depth + 1, bridge, null, list, rand);
					}
				}
				add(list, p, -1, 4, -1, "tower_top", rot, true);
			} else {
				if (depth != 7) return recursiveChildren(this.fatTower, depth + 1, p, null, list, rand);
				add(list, p, -1, 4, -1, "tower_top", rot, true);
			}
			return true;
		};

		final Generator fatTower = (depth, parent, pos, list, rand) -> {
			int rot = parent.rotation;
			Piece p = add(list, parent, -3, 4, -3, "fat_tower_base", rot, true);
			p = add(list, p, 0, 4, 0, "fat_tower_middle", rot, true);
			for (int i = 0; i < 2 && rand.nextInt(3) != 0; i++) {
				p = add(list, p, 0, 8, 0, "fat_tower_middle", rot, true);
				for (int[] b : FAT_TOWER_BRIDGES) {
					if (rand.nextBoolean()) {
						Piece bridge = add(list, p, b[1], b[2], b[3], "bridge_end", rot + b[0], true);
						recursiveChildren(this.towerBridge, depth + 1, bridge, null, list, rand);
					}
				}
			}
			add(list, p, -2, 8, -2, "fat_tower_top", rot, true);
			return true;
		};
	}

	/**
	 * Builds a whole city starting at (x, y, z), like vanilla's startHouseTower.
	 */
	public static List<Piece> build(int x, int y, int z, int rotation, Random rand) {
		boolean wantShip = rand.nextInt(SHIP_CHANCE) < SHIP_ROLLS;
		List<Piece> pieces = null;
		// A layout can lose its ship to overlaps or have no bridge to hang it from, so try a few layouts
		for (int attempt = 0; attempt < 6; attempt++) {
			pieces = buildOnce(x, y, z, rotation, rand, wantShip);
			if (!wantShip || hasShip(pieces)) break;
		}
		return pieces;
	}

	private static boolean hasShip(List<Piece> pieces) {
		for (Piece p : pieces) {
			if (p.name.equals("ship")) return true;
		}
		return false;
	}

	private static List<Piece> buildOnce(int x, int y, int z, int rotation, Random rand, boolean wantShip) {
		Builder b = new Builder(wantShip);
		Piece root = new Piece("base_floor", x, y, z, rotation, true);
		b.pieces.add(root);
		Piece p = b.add(b.pieces, root, -1, 0, -1, "second_floor_1", rotation, false);
		p = b.add(b.pieces, p, -1, 4, -1, "third_floor_1", rotation, false);
		p = b.add(b.pieces, p, -1, 8, -1, "third_roof", rotation, true);
		b.recursiveChildren(b.tower, 1, p, null, b.pieces, rand);
		return b.pieces;
	}
}
