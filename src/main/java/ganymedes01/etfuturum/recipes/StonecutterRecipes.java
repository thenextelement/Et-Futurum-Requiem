package ganymedes01.etfuturum.recipes;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.*;

/**
 * Stonecutter recipes, derived from the crafting recipes the same way vanilla's relate to crafting:
 * one stone or copper full block cut into its slabs (2 each), stairs, walls and other full blocks (1 each),
 * followed through 1:1 full-block steps so e.g. stone gives stone brick stairs directly.
 * Built lazily so every mod's recipes are included; ordered by registry name so client and server agree.
 */
public final class StonecutterRecipes {

	private static final int MAX_CHAIN = 3;
	private static final List<ItemStack> EXTRA_INPUTS = new ArrayList<>();
	private static final List<ItemStack> EXTRA_OUTPUTS = new ArrayList<>();

	private static Map<String, List<ItemStack>> recipes;

	private StonecutterRecipes() {
	}

	/**
	 * API for other mods/compat: add a stonecutter recipe that can't be derived from crafting.
	 */
	public static void addRecipe(ItemStack input, ItemStack output) {
		EXTRA_INPUTS.add(input.copy());
		EXTRA_OUTPUTS.add(output.copy());
		recipes = null;
	}

	public static List<ItemStack> getOutputs(ItemStack input) {
		if (input == null || input.getItem() == null) return Collections.emptyList();
		if (recipes == null) build();
		List<ItemStack> list = recipes.get(key(input.getItem(), input.getItemDamage()));
		if (list == null) list = recipes.get(key(input.getItem(), OreDictionary.WILDCARD_VALUE));
		return list == null ? Collections.emptyList() : list;
	}

	public static boolean hasRecipes(ItemStack input) {
		return !getOutputs(input).isEmpty();
	}

	private static String key(Item item, int meta) {
		return GameData.getItemRegistry().getNameForObject(item) + "@" + meta;
	}

	private static void build() {
		Map<String, ItemStack> inputStacks = new HashMap<>();
		Map<String, Map<String, ItemStack>> direct = new HashMap<>();

		for (Object o : CraftingManager.getInstance().getRecipeList()) {
			IRecipe recipe = (IRecipe) o;
			ItemStack output = recipe.getRecipeOutput();
			if (!isValidOutput(output)) continue;
			List<Object> ingredients = getIngredients(recipe);
			if (ingredients == null || ingredients.isEmpty()) continue;

			List<ItemStack> candidates = getSharedIngredient(ingredients);
			if (candidates == null) continue;
			int count = Math.max(1, output.stackSize / ingredients.size());
			for (ItemStack in : candidates) {
				if (!isValidInput(in) || (in.getItem() == output.getItem() && in.getItemDamage() == output.getItemDamage())) continue;
				String inKey = key(in.getItem(), in.getItemDamage());
				inputStacks.put(inKey, in);
				ItemStack out = output.copy();
				out.stackSize = count;
				direct.computeIfAbsent(inKey, k -> new LinkedHashMap<>()).putIfAbsent(key(out.getItem(), out.getItemDamage()), out);
			}
		}
		for (int i = 0; i < EXTRA_INPUTS.size(); i++) {
			ItemStack in = EXTRA_INPUTS.get(i), out = EXTRA_OUTPUTS.get(i);
			String inKey = key(in.getItem(), in.getItemDamage());
			inputStacks.put(inKey, in);
			direct.computeIfAbsent(inKey, k -> new LinkedHashMap<>()).put(key(out.getItem(), out.getItemDamage()), out);
		}

		Map<String, List<ItemStack>> built = new HashMap<>();
		for (String inKey : direct.keySet()) {
			Map<String, ItemStack> all = new TreeMap<>();
			collect(direct, inKey, 1, all, new HashSet<>(Collections.singleton(inKey)), 0);
			all.remove(inKey);
			built.put(inKey, Collections.unmodifiableList(new ArrayList<>(all.values())));
		}
		recipes = built;
	}

	private static void collect(Map<String, Map<String, ItemStack>> direct, String from, int multiplier, Map<String, ItemStack> into, Set<String> visited, int depth) {
		Map<String, ItemStack> outs = direct.get(from);
		if (outs == null || depth >= MAX_CHAIN) return;
		for (Map.Entry<String, ItemStack> e : outs.entrySet()) {
			ItemStack out = e.getValue().copy();
			out.stackSize = Math.min(out.getMaxStackSize(), out.stackSize * multiplier);
			into.putIfAbsent(e.getKey(), out);
			// Only keep chaining through 1:1 full blocks (stone -> stone bricks -> stone brick stairs)
			if (e.getValue().stackSize == 1 && isFullBlock(e.getValue()) && visited.add(e.getKey())) {
				collect(direct, e.getKey(), multiplier, into, visited, depth + 1);
			}
		}
	}

	private static List<Object> getIngredients(IRecipe recipe) {
		List<Object> list = new ArrayList<>();
		if (recipe instanceof ShapedRecipes r) {
			Collections.addAll(list, (Object[]) r.recipeItems);
		} else if (recipe instanceof ShapedOreRecipe r) {
			Collections.addAll(list, r.getInput());
		} else if (recipe instanceof ShapelessRecipes r) {
			list.addAll(r.recipeItems);
		} else if (recipe instanceof ShapelessOreRecipe r) {
			list.addAll(r.getInput());
		} else {
			return null;
		}
		list.removeIf(Objects::isNull);
		return list;
	}

	/**
	 * @return the possible stacks if every ingredient is the same thing, otherwise null
	 */
	@SuppressWarnings("unchecked")
	private static List<ItemStack> getSharedIngredient(List<Object> ingredients) {
		Object first = ingredients.get(0);
		for (Object o : ingredients) {
			if (first instanceof ItemStack a && o instanceof ItemStack b) {
				if (a.getItem() != b.getItem() || a.getItemDamage() != b.getItemDamage()) return null;
			} else if (o != first && !o.equals(first)) {
				return null;
			}
		}
		if (first instanceof ItemStack stack) return Collections.singletonList(stack);
		if (first instanceof List<?> list && !list.isEmpty()) return (List<ItemStack>) list;
		return null;
	}

	private static boolean isCuttableMaterial(Block block) {
		return block.getMaterial() == Material.rock || block.getMaterial() == Material.iron;
	}

	private static boolean isValidInput(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemBlock)) return false;
		Block block = Block.getBlockFromItem(stack.getItem());
		return block != Blocks.air && isCuttableMaterial(block) && isFullBlock(stack);
	}

	private static boolean isValidOutput(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) return false;
		Block block = Block.getBlockFromItem(stack.getItem());
		if (block == Blocks.air || !isCuttableMaterial(block) || block.hasTileEntity(stack.getItemDamage())) return false;
		return block instanceof BlockSlab || block instanceof BlockStairs || block instanceof BlockWall || isFullBlock(stack);
	}

	private static boolean isFullBlock(ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		return !(block instanceof BlockSlab) && !(block instanceof BlockStairs) && !(block instanceof BlockWall)
				&& block.isOpaqueCube() && block.renderAsNormalBlock();
	}
}
