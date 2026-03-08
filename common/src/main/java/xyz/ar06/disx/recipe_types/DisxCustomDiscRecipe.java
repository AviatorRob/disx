package xyz.ar06.disx.recipe_types;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.items.DisxBlankDisc;
import xyz.ar06.disx.items.DisxRecordStamp;
import com.google.gson.JsonObject;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class DisxCustomDiscRecipe implements Recipe<Container> {
    private final ItemStack result;
    private final Item decorativeFactor;
    private final String name;
    public DisxCustomDiscRecipe(ItemStack result, Item decorativeFactor, String name) {
        this.result = result;
        this.decorativeFactor = decorativeFactor;
        this.name = name;
    }

    public Item getDecorativeFactor() {
        return decorativeFactor;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getItem(0).getItem().equals(DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx","blank_disc")))){
            if (container.getItem(2).getItem().equals(this.decorativeFactor)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return this.result;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        if (i == 2 && j == 1){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("disx","custom_disc_recipe_" + name);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DisxCustomDiscRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return DisxCustomDiscRecipeType.INSTANCE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(DisxBlankDisc.itemRegistration.get()));
        list.add(Ingredient.of(this::getDecorativeFactor));
        list.add(Ingredient.of(DisxRecordStamp.getItemRegistration().get()));
        return list;
    }

    public static class DisxCustomDiscRecipeType implements RecipeType<DisxCustomDiscRecipe> {
        private DisxCustomDiscRecipeType() {}
        public static final DisxCustomDiscRecipeType INSTANCE = new DisxCustomDiscRecipeType();

        public static void registerRecipeType(Registrar<RecipeType<?>> registrar){
            registrar.register(new ResourceLocation("disx","custom_disc_recipe"),
                    () -> INSTANCE);
        }
    }

    public static class DisxCustomDiscRecipeSerializer implements RecipeSerializer<DisxCustomDiscRecipe> {

        public static DisxCustomDiscRecipeSerializer INSTANCE = new DisxCustomDiscRecipeSerializer();
        @Override
        public DisxCustomDiscRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            String decorativeFactor = jsonObject.get("decorative_factor").getAsString();
            String name = jsonObject.get("name").getAsString();
            JsonObject resultObject = jsonObject.getAsJsonObject("result");
            String itemLocation = resultObject.get("item").getAsString();
            ItemStack resultItem = new ItemStack(DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation(itemLocation)));
            Item decorativeFactorItem = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation(decorativeFactor));
            return new DisxCustomDiscRecipe(resultItem, decorativeFactorItem, name);
        }

        @Override
        public DisxCustomDiscRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            ItemStack result = friendlyByteBuf.readItem();
            ItemStack decorativeFactor = friendlyByteBuf.readItem();
            String name = friendlyByteBuf.readUtf();
            return new DisxCustomDiscRecipe(result, decorativeFactor.getItem(), name);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, DisxCustomDiscRecipe recipe) {
            friendlyByteBuf.writeItem(recipe.getResultItem());
            friendlyByteBuf.writeItem(new ItemStack(recipe.getDecorativeFactor()));
            friendlyByteBuf.writeUtf(recipe.getName());
        }

        public static void registerRecipeSerializer(Registrar<RecipeSerializer<?>> registrar){
            registrar.register(new ResourceLocation("disx","custom_disc_recipe"),
                    () -> INSTANCE);
        }
    }

}
