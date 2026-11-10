package ganymedes01.etfuturum.world.end;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.world.end.dimension.WorldProviderEFREnd;
import ganymedes01.etfuturum.world.end.gen.WorldGenEndGateway;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Opens one of the 20 main-island End gateways each time a dragon dies, like 1.9+.
 */
public class EndGatewaySpawner {

	public static final EndGatewaySpawner INSTANCE = new EndGatewaySpawner();

	private static final int GATEWAY_COUNT = 20;
	private static final int RADIUS = 96;
	private static final int HEIGHT = 75;

	@SubscribeEvent
	public void onDragonDeath(LivingDeathEvent event) {
		if (!(event.entityLiving instanceof EntityDragon)) return;
		World world = event.entityLiving.worldObj;
		if (world.isRemote || !(world.provider instanceof WorldProviderEFREnd)) return;

		GatewayData data = GatewayData.get(world);
		if (data.remaining.isEmpty()) return;
		int index = data.remaining.remove(data.remaining.size() - 1);
		data.markDirty();

		double angle = 2.0D * Math.PI * index / GATEWAY_COUNT;
		int x = MathHelper.floor_double(RADIUS * Math.cos(angle));
		int z = MathHelper.floor_double(RADIUS * Math.sin(angle));
		new WorldGenEndGateway().generate(world, world.rand, x, HEIGHT, z);
	}

	public static class GatewayData extends WorldSavedData {
		private static final String NAME = "etfuturum_end_gateways";
		private final List<Integer> remaining = new ArrayList<>();

		public GatewayData(String name) {
			super(name);
		}

		static GatewayData get(World world) {
			GatewayData data = (GatewayData) world.perWorldStorage.loadData(GatewayData.class, NAME);
			if (data == null) {
				data = new GatewayData(NAME);
				for (int i = 0; i < GATEWAY_COUNT; i++) data.remaining.add(i);
				Collections.shuffle(data.remaining, new Random(world.getSeed()));
				world.perWorldStorage.setData(NAME, data);
				data.markDirty();
			}
			return data;
		}

		@Override
		public void readFromNBT(NBTTagCompound tag) {
			remaining.clear();
			for (int i : tag.getIntArray("Remaining")) remaining.add(i);
		}

		@Override
		public void writeToNBT(NBTTagCompound tag) {
			int[] arr = new int[remaining.size()];
			for (int i = 0; i < arr.length; i++) arr[i] = remaining.get(i);
			tag.setIntArray("Remaining", arr);
		}
	}
}
