package ganymedes01.etfuturum.client.renderer.item;

import ganymedes01.etfuturum.client.ShieldPatternTexture;
import ganymedes01.etfuturum.client.model.ModelShield;
import ganymedes01.etfuturum.items.equipment.ItemShield;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ItemShieldRenderer implements IItemRenderer {

	private static final ResourceLocation PLAIN = new ResourceLocation("textures/entity/shield_base_nopattern.png");
	private static final ResourceLocation BASE = new ResourceLocation("textures/entity/shield_base.png");
	private static final Map<String, Cached> TEXTURES = new HashMap<>();

	/**
	 * GUI and dropped: translate x, y, z, rotate x, y, z, scale, then model-space x and y signs that undo the mirroring
	 * those frames carry so banner designs read the right way round.
	 */
	public static final float[] GUI = {8, 8, 0, 15, 155, -5, 10.4F, -1, 1};
	public static final float[] GROUND = {0, 0, 0, 0, 0, 0, 0.6F, -1, -1};

	/**
	 * Modern display transforms from the official shield.json / shield_blocking.json: translation (pixels), rotation
	 * (degrees), scale. Right hand values; vanilla's left hand rule is applied on top for the offhand.
	 */
	private static final float[] FP_RIGHT = {-10, 2, -10, 0, 180, 5, 1.25F};
	private static final float[] FP_LEFT = {10, 0, -10, 0, 180, 5, 1.25F};
	private static final float[] FP_RIGHT_BLOCKING = {-15, 5, -11, 0, 180, -5, 1.25F};
	private static final float[] FP_LEFT_BLOCKING = {5, 5, -11, 0, 180, -5, 1.25F};
	private static final float[] TP_RIGHT = {10, 6, -4, 0, 90, 0, 1};
	private static final float[] TP_LEFT = {10, 6, 12, 0, 90, 0, 1};
	private static final float[] TP_RIGHT_BLOCKING = {-3.49F, 11, -2, 45, 155, 0, 1};
	private static final float[] TP_LEFT_BLOCKING = {11.51F, 7, 2.5F, 45, 155, 0, 1};

	private static final java.nio.FloatBuffer MATRIX = org.lwjgl.BufferUtils.createFloatBuffer(16);

	private final ModelShield model = new ModelShield();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type != ItemRenderType.FIRST_PERSON_MAP;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY && (helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION);
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		EntityPlayer player = data.length > 1 && data[1] instanceof EntityPlayer p ? p : null;
		boolean blocking = player != null && player.getItemInUseCount() > 0
				&& (player.getItemInUse() == stack || ItemStack.areItemStacksEqual(player.getItemInUse(), stack));
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_POLYGON_BIT);
		switch (type) {
			case INVENTORY -> {
				// Inventory items aren't depth tested; cull back faces and draw the handle first so the plate covers it
				GL11.glEnable(GL11.GL_CULL_FACE);
				apply(GUI);
				RenderHelper.enableGUIStandardItemLighting();
			}
			case EQUIPPED_FIRST_PERSON -> {
				boolean left = isMirrored();
				firstPersonFrame(player, left);
				modern(left ? (blocking ? FP_LEFT_BLOCKING : FP_LEFT) : (blocking ? FP_RIGHT_BLOCKING : FP_RIGHT), left);
			}
			case EQUIPPED -> {
				boolean left = isMirrored();
				if (player != null) thirdPersonFrame(player, left);
				// Modern ItemInHandLayer, from the arm's frame
				GL11.glRotatef(-90.0F, 1, 0, 0);
				GL11.glRotatef(180.0F, 0, 1, 0);
				GL11.glTranslatef((left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
				modern(left ? (blocking ? TP_LEFT_BLOCKING : TP_LEFT) : (blocking ? TP_RIGHT_BLOCKING : TP_RIGHT), left);
			}
			default -> apply(GROUND);
		}
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture(stack));
		model.render();
		if (type == ItemRenderType.INVENTORY) RenderHelper.disableStandardItemLighting();
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private static void apply(float[] p) {
		GL11.glTranslatef(p[0], p[1], p[2]);
		GL11.glRotatef(p[3], 1, 0, 0);
		GL11.glRotatef(p[4], 0, 1, 0);
		GL11.glRotatef(p[5], 0, 0, 1);
		GL11.glScalef(p[6] * p[7], p[6] * p[8], p[6]);
	}

	/**
	 * Modern ItemTransform.apply (with its left hand rule), then the special model's -0.5 offset and the shield
	 * renderer's (1, -1, -1) scale.
	 */
	private static void modern(float[] t, boolean left) {
		GL11.glTranslatef((left ? -t[0] : t[0]) / 16.0F, t[1] / 16.0F, t[2] / 16.0F);
		GL11.glRotatef(t[3], 1, 0, 0);
		GL11.glRotatef(left ? -t[4] : t[4], 0, 1, 0);
		GL11.glRotatef(left ? -t[5] : t[5], 0, 0, 1);
		GL11.glScalef(t[6], t[6], t[6]);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		GL11.glCullFace(GL11.GL_BACK);
	}

	/**
	 * Backhand draws the offhand in a mirrored frame in both views.
	 */
	private static boolean isMirrored() {
		MATRIX.clear();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MATRIX);
		float a = MATRIX.get(0), b = MATRIX.get(4), c = MATRIX.get(8);
		float d = MATRIX.get(1), e = MATRIX.get(5), f = MATRIX.get(9);
		float g = MATRIX.get(2), h = MATRIX.get(6), i = MATRIX.get(10);
		return a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g) < 0;
	}

	/**
	 * Rebuilds the first person hand frame the way modern does (view bobbing, arm sway, applyItemArmTransform)
	 * instead of using 1.7.10's sword-oriented one.
	 */
	private static void firstPersonFrame(EntityPlayer player, boolean left) {
		Minecraft mc = Minecraft.getMinecraft();
		float partial = mc.timer.renderPartialTicks;
		GL11.glLoadIdentity();
		if (mc.gameSettings.viewBobbing && mc.renderViewEntity instanceof EntityPlayer p) {
			float walked = p.distanceWalkedModified - p.prevDistanceWalkedModified;
			float f2 = -(p.distanceWalkedModified + walked * partial);
			float f3 = p.prevCameraYaw + (p.cameraYaw - p.prevCameraYaw) * partial;
			float f4 = p.prevCameraPitch + (p.cameraPitch - p.prevCameraPitch) * partial;
			GL11.glTranslatef(MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F);
			GL11.glRotatef(MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0, 0, 1);
			GL11.glRotatef(Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1, 0, 0);
			GL11.glRotatef(f4, 1, 0, 0);
		}
		if (player instanceof net.minecraft.client.entity.EntityPlayerSP sp) {
			float armPitch = sp.prevRenderArmPitch + (sp.renderArmPitch - sp.prevRenderArmPitch) * partial;
			float armYaw = sp.prevRenderArmYaw + (sp.renderArmYaw - sp.prevRenderArmYaw) * partial;
			GL11.glRotatef((sp.rotationPitch - armPitch) * 0.1F, 1, 0, 0);
			GL11.glRotatef((sp.rotationYaw - armYaw) * 0.1F, 0, 1, 0);
		}
		int side = left ? -1 : 1;
		if (!left && player != null && !player.isUsingItem()) {
			float swing = player.getSwingProgress(partial);
			float sq = MathHelper.sqrt_float(swing);
			GL11.glTranslatef(side * -0.4F * MathHelper.sin(sq * (float) Math.PI), 0.2F * MathHelper.sin(sq * (float) Math.PI * 2.0F), -0.2F * MathHelper.sin(swing * (float) Math.PI));
			GL11.glTranslatef(side * 0.56F, -0.52F, -0.72F);
			float f = MathHelper.sin(swing * swing * (float) Math.PI);
			GL11.glRotatef(side * (45.0F + f * -20.0F), 0, 1, 0);
			float f1 = MathHelper.sin(sq * (float) Math.PI);
			GL11.glRotatef(side * f1 * -20.0F, 0, 0, 1);
			GL11.glRotatef(f1 * -80.0F, 1, 0, 0);
			GL11.glRotatef(side * -45.0F, 0, 1, 0);
		} else {
			GL11.glTranslatef(side * 0.56F, -0.52F, -0.72F);
		}
	}

	/**
	 * Takes RenderPlayer's (or Backhand's mirrored) held item transforms back off, leaving the arm's own frame,
	 * which is what modern's translateToHand gives.
	 */
	private static void thirdPersonFrame(EntityPlayer player, boolean left) {
		// ForgeHooksClient.renderEquippedItem's own transforms for custom held renderers
		GL11.glTranslatef(0.9375F, 0.0625F, 0.0F);
		GL11.glRotatef(-335.0F, 0, 0, 1);
		GL11.glRotatef(-50.0F, 0, 1, 0);
		GL11.glScalef(1 / 1.5F, 1 / 1.5F, 1 / 1.5F);
		GL11.glTranslatef(0.0F, 0.3F, 0.0F);
		float s = 0.625F;
		GL11.glRotatef(-45.0F, 0, 1, 0);
		GL11.glRotatef(100.0F, 1, 0, 0);
		GL11.glScalef(1 / s, -1 / s, 1 / s);
		GL11.glTranslatef(0.0F, -0.1875F, 0.0F);
		if (player.getItemInUseCount() > 0) {
			GL11.glRotatef(60.0F, 0, 0, 1);
			GL11.glRotatef(10.0F, 1, 0, 0);
			GL11.glRotatef(50.0F, 0, 1, 0);
			GL11.glTranslatef(-0.05F, 0.0F, 0.1F);
		}
		GL11.glTranslatef(0.0625F, -0.4375F, -0.0625F);
		if (left) {
			float px = 5, py = 2, pz = 0;
			if (net.minecraft.client.renderer.entity.RenderManager.instance.getEntityRenderObject(player) instanceof net.minecraft.client.renderer.entity.RenderPlayer rp) {
				px = rp.modelBipedMain.bipedLeftArm.rotationPointX;
				py = rp.modelBipedMain.bipedLeftArm.rotationPointY;
				pz = rp.modelBipedMain.bipedLeftArm.rotationPointZ;
			}
			GL11.glTranslatef(px * 0.0625F, -py * 0.0625F, pz * 0.0625F);
			GL11.glScalef(-1, 1, 1);
			GL11.glTranslatef(px * 0.0625F, py * 0.0625F, pz * 0.0625F);
		}
	}

	private static ResourceLocation getTexture(ItemStack stack) {
		NBTTagCompound tag = ItemShield.getBannerTag(stack);
		if (tag == null) return PLAIN;
		List<ResourceLocation> masks = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		StringBuilder key = new StringBuilder("b").append(tag.getInteger("Base"));
		masks.add(new ResourceLocation("textures/entity/shield/base.png"));
		colors.add(tag.getInteger("Base"));
		NBTTagList patterns = tag.getTagList("Patterns", 10);
		for (int i = 0; i < patterns.tagCount() && i < 6; i++) {
			NBTTagCompound p = patterns.getCompoundTagAt(i);
			EnumBannerPattern pattern = EnumBannerPattern.getPatternByID(p.getString("Pattern"));
			if (pattern == null) continue;
			masks.add(new ResourceLocation("textures/entity/shield/" + pattern.getPatternName() + ".png"));
			colors.add(p.getInteger("Color"));
			key.append(pattern.getPatternID()).append(p.getInteger("Color"));
		}

		String k = key.toString();
		Cached cached = TEXTURES.get(k);
		if (cached == null) {
			if (TEXTURES.size() >= 256) evictOld();
			cached = new Cached(new ResourceLocation("etfuturum", "shield/" + k));
			Minecraft.getMinecraft().getTextureManager().loadTexture(cached.location, new ShieldPatternTexture(BASE, masks, colors));
			TEXTURES.put(k, cached);
		}
		cached.time = System.currentTimeMillis();
		return cached.location;
	}

	private static void evictOld() {
		long now = System.currentTimeMillis();
		Iterator<Cached> it = TEXTURES.values().iterator();
		while (it.hasNext()) {
			Cached c = it.next();
			if (now - c.time > 60000L) {
				Minecraft.getMinecraft().getTextureManager().deleteTexture(c.location);
				it.remove();
			}
		}
	}

	private static final class Cached {
		final ResourceLocation location;
		long time;

		Cached(ResourceLocation location) {
			this.location = location;
		}
	}
}
