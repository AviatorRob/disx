package xyz.ar06.disx.recipe_types;

import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.items.DisxRecordStamp;
import com.google.gson.JsonObject;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

//DUMMY RECIPE FOR REI COMPATIBILITY; ONLY MADE TO RETURN 1 INGREDIENT AND 1 RESULT
public class DisxStampRecipe implements Recipe<Container> {

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(DisxRecordStamp.getItemRegistration().get());
    }

    public ItemStack getResultItem(){
        return new ItemStack(DisxRecordStamp.getItemRegistration().get());
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("disx","stamp_maker_recipe");
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DisxStampRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return DisxStampRecipeType.INSTANCE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(DisxLacquerBlock.itemRegistration.get()));
        return list;
    }

    public static class DisxStampRecipeType implements RecipeType<DisxStampRecipe>{
        private DisxStampRecipeType(){

        }

        public static final DisxStampRecipeType INSTANCE = new DisxStampRecipeType();

        public static void registerRecipeType(Registrar<RecipeType<?>> registrar){
            registrar.register(new ResourceLocation("disx","stamp_maker_recipe"),
                    () -> INSTANCE);
        }
    }

    public static class DisxStampRecipeSerializer implements RecipeSerializer<DisxStampRecipe>{
        public static final DisxStampRecipeSerializer INSTANCE = new DisxStampRecipeSerializer();

        @Override
        public DisxStampRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            return new DisxStampRecipe();
        }

        @Override
        public DisxStampRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            return new DisxStampRecipe();
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, DisxStampRecipe recipe) {

        }

        public static void registerSerializer(Registrar<RecipeSerializer<?>> registrar){
            registrar.register(new ResourceLocation("disx","stamp_maker_recipe"),
                    () -> INSTANCE);
        }
    }

}
