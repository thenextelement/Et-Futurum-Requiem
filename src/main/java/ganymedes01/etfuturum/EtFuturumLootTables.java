package ganymedes01.etfuturum;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import java.util.Random;

public class EtFuturumLootTables {

	private static final String COMPOSTER = "composting";
	public static final ChestGenHooks COMPOSTER_LOOT = ChestGenHooks.getInfo(COMPOSTER);

	private static final String END_CITY = "etfuturum:end_city_treasure";
	public static final ChestGenHooks END_CITY_TREASURE = ChestGenHooks.getInfo(END_CITY);

	public static void init() {
		ChestGenHooks.getInfo(COMPOSTER).setMin(1);
		ChestGenHooks.getInfo(COMPOSTER).setMax(1);
		COMPOSTER_LOOT.addItem(new WeightedRandomChestContent(new ItemStack(Items.dye, 1, 15), 1, 1, 10));
		//For some reason it is min count, max count and weight, yes weight is the last arg not the first....

		// Richer than vanilla's end_city_treasure: more rolls, diamond-only gear with stronger enchantments
		END_CITY_TREASURE.setMin(4);
		END_CITY_TREASURE.setMax(8);
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.diamond), 3, 8, 10));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.emerald), 2, 6, 5));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.gold_ingot), 3, 8, 8));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.iron_ingot), 4, 9, 6));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.ender_pearl), 2, 6, 6));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.experience_bottle), 4, 12, 6));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.golden_apple), 1, 3, 4));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.golden_apple, 1, 1), 1, 1, 1));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(ModItems.BEETROOT_SEEDS.isEnabled() ? ModItems.BEETROOT_SEEDS.newItemStack() : new ItemStack(Items.wheat_seeds), 1, 10, 2));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.saddle), 1, 1, 2));
		END_CITY_TREASURE.addItem(new WeightedRandomChestContent(new ItemStack(Items.diamond_horse_armor), 1, 1, 2));
		END_CITY_TREASURE.addItem(new EnchantedChestContent(new ItemStack(Items.book), 6));
		for (Item gear : new Item[]{Items.diamond_sword, Items.diamond_axe, Items.diamond_pickaxe, Items.diamond_shovel,
				Items.diamond_helmet, Items.diamond_chestplate, Items.diamond_leggings, Items.diamond_boots}) {
			END_CITY_TREASURE.addItem(new EnchantedChestContent(new ItemStack(gear), 4));
		}
	}

	/**
	 * Enchanted as if at a level 30-39 table (books turn into enchanted books).
	 */
	private static class EnchantedChestContent extends WeightedRandomChestContent {
		EnchantedChestContent(ItemStack stack, int weight) {
			super(stack, 1, 1, weight);
		}

		@Override
		protected ItemStack[] generateChestContent(Random random, net.minecraft.inventory.IInventory newInventory) {
			ItemStack stack = theItemId.copy();
			EnchantmentHelper.addRandomEnchantment(random, stack, 30 + random.nextInt(10));
			return new ItemStack[]{stack};
		}
	}
}
