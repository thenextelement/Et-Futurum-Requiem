package ganymedes01.etfuturum.client.renderer.block;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Draws boxes modelled pointing UP (in 1/16 block units, with per-face UVs in texture pixels) rotated to any facing.
 */
public final class OrientedBoxRenderer {

	private final Tessellator t = Tessellator.instance;
	private ForgeDirection facing = ForgeDirection.UP;
	private double ox, oy, oz;
	private boolean worldLighting;
	private IIcon icon;

	public OrientedBoxRenderer begin(double x, double y, double z, ForgeDirection facing, IIcon icon, boolean worldLighting) {
		this.ox = x;
		this.oy = y;
		this.oz = z;
		this.facing = facing;
		this.icon = icon;
		this.worldLighting = worldLighting;
		return this;
	}

	/**
	 * @param sideUV  {u0, v0, u1, v1} in pixels for the four sides
	 * @param endUV   {u0, v0, u1, v1} in pixels for the top and bottom
	 */
	public void box(double x0, double y0, double z0, double x1, double y1, double z1, double[] sideUV, double[] endUV) {
		x0 /= 16; y0 /= 16; z0 /= 16; x1 /= 16; y1 /= 16; z1 /= 16;
		double su0 = icon.getInterpolatedU(sideUV[0]), sv0 = icon.getInterpolatedV(sideUV[1]);
		double su1 = icon.getInterpolatedU(sideUV[2]), sv1 = icon.getInterpolatedV(sideUV[3]);
		double eu0 = icon.getInterpolatedU(endUV[0]), ev0 = icon.getInterpolatedV(endUV[1]);
		double eu1 = icon.getInterpolatedU(endUV[2]), ev1 = icon.getInterpolatedV(endUV[3]);

		// Y-
		face(0, -1, 0);
		v(x0, y0, z1, eu0, ev1); v(x0, y0, z0, eu0, ev0); v(x1, y0, z0, eu1, ev0); v(x1, y0, z1, eu1, ev1);
		// Y+
		face(0, 1, 0);
		v(x1, y1, z1, eu1, ev1); v(x1, y1, z0, eu1, ev0); v(x0, y1, z0, eu0, ev0); v(x0, y1, z1, eu0, ev1);
		// Z-
		face(0, 0, -1);
		v(x0, y1, z0, su1, sv0); v(x1, y1, z0, su0, sv0); v(x1, y0, z0, su0, sv1); v(x0, y0, z0, su1, sv1);
		// Z+
		face(0, 0, 1);
		v(x0, y1, z1, su0, sv0); v(x0, y0, z1, su0, sv1); v(x1, y0, z1, su1, sv1); v(x1, y1, z1, su1, sv0);
		// X-
		face(-1, 0, 0);
		v(x0, y1, z1, su1, sv0); v(x0, y1, z0, su0, sv0); v(x0, y0, z0, su0, sv1); v(x0, y0, z1, su1, sv1);
		// X+
		face(1, 0, 0);
		v(x1, y0, z1, su0, sv1); v(x1, y0, z0, su1, sv1); v(x1, y1, z0, su1, sv0); v(x1, y1, z1, su0, sv0);
	}

	private void face(double nx, double ny, double nz) {
		double[] n = rotate(nx, ny, nz, false);
		if (worldLighting) {
			float shade = n[1] > 0.5 ? 1.0F : n[1] < -0.5 ? 0.5F : Math.abs(n[2]) > 0.5 ? 0.8F : 0.6F;
			t.setColorOpaque_F(shade, shade, shade);
		} else {
			t.setNormal((float) n[0], (float) n[1], (float) n[2]);
		}
	}

	private void v(double x, double y, double z, double u, double v) {
		double[] p = rotate(x, y, z, true);
		t.addVertexWithUV(ox + p[0], oy + p[1], oz + p[2], u, v);
	}

	/**
	 * Proper rotations (determinant +1) taking the model's +Y axis to the facing, so face winding is preserved.
	 */
	private double[] rotate(double x, double y, double z, boolean point) {
		double c = point ? 1 : 0; // translation part only applies to points
		return switch (facing) {
			case DOWN -> new double[]{x, c - y, c - z};
			case NORTH -> new double[]{x, z, c - y};
			case SOUTH -> new double[]{x, c - z, y};
			case WEST -> new double[]{c - y, x, z};
			case EAST -> new double[]{y, c - x, z};
			default -> new double[]{x, y, z};
		};
	}
}
