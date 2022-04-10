package design.aeonic.multitool.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import design.aeonic.multitool.EngineersMultitool;
import design.aeonic.multitool.api.structure.BuildableStructure;
import design.aeonic.multitool.registry.EMRecipeTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record StructureBuildingRecipe(ResourceLocation recipeId, BuildableStructure structure, List<Ingredient> ingredients) implements Recipe<Container> {
    public static Serializer SERIALIZER = new Serializer();

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        List<ItemStack> inputs = new ArrayList<>();
        for(int i = 0; i < pContainer.getContainerSize(); i++) {
            inputs.add(pContainer.getItem(i));
        }

        return RecipeMatcher.findMatches(inputs,  this.ingredients) != null;
    }

    @Override
    public ItemStack assemble(Container pContainer) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeId();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return EMRecipeTypes.MULTITOOL_BUILDING.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<StructureBuildingRecipe> {
        @Override
        public StructureBuildingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients"));
            BuildableStructure structure = BuildableStructure.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(pSerializedRecipe, "structure")).getOrThrow(false, EngineersMultitool.LOGGER::error);
            return new StructureBuildingRecipe(pRecipeId, structure, ingredients);
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray pIngredientArray) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for(var ingredientJson: pIngredientArray) {
                Ingredient ingredient = Ingredient.fromJson(ingredientJson);
                if (!ingredient.isEmpty())
                    ingredients.add(ingredient);
            }
            return ingredients;
        }

        @Nullable
        @Override
        public StructureBuildingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            BuildableStructure structure = pBuffer.readWithCodec(BuildableStructure.CODEC);
            int size = pBuffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.fromNetwork(pBuffer));
            }
            return new StructureBuildingRecipe(pRecipeId, structure, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, StructureBuildingRecipe pRecipe) {
            pBuffer.writeWithCodec(BuildableStructure.CODEC, pRecipe.structure());
            pBuffer.writeVarInt(pRecipe.ingredients().size());
            for (var ingredient: pRecipe.ingredients()) {
                ingredient.toNetwork(pBuffer);
            }
        }
    }

    public static class Builder {
        private final BuildableStructure structure;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final Advancement.Builder advancement = Advancement.Builder.advancement();

        public Builder(BuildableStructure structure) {
            this.structure = structure;
        }

        public static Builder get(BuildableStructure structure) {
            return new Builder(structure);
        }

        public Builder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
            this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
            return this;
        }

        /**
         * Adds an ingredient that can be any item in the given tag.
         */
        public Builder requires(Tag<Item> pTag) {
            return this.requires(Ingredient.of(pTag));
        }

        /**
         * Adds an ingredient of the given item.
         */
        public Builder requires(ItemLike pItem) {
            return requires(pItem, 1);
        }

        /**
         * Adds the given ingredient multiple times.
         */
        public Builder requires(ItemLike pItem, int pQuantity) {
            return requires(Ingredient.of(pItem), pQuantity);
        }

        /**
         * Adds an ingredient.
         */
        public Builder requires(Ingredient ingredient) {
            return requires(ingredient, 1);
        }

        /**
         * Adds the given ingredient multiple times.
         */
        public Builder requires(Ingredient ingredient, int count) {
            for (int i = 0; i < count; i++)
                ingredients.add(ingredient);
            return this;
        }

        public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
            this.advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
            pFinishedRecipeConsumer.accept(new Result(pRecipeId, structure, ingredients, advancement, new ResourceLocation(pRecipeId.getNamespace(), "recipes/multitool/building/" + pRecipeId.getPath())));
        }

        public record Result(ResourceLocation id, BuildableStructure structure, List<Ingredient> ingredients, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {
            @Override
            public void serializeRecipeData(JsonObject pJson) {
                JsonArray ingredients = new JsonArray();
                for (var ingredient: ingredients())
                    ingredients.add(ingredient.toJson());

                JsonElement structure = BuildableStructure.CODEC.encodeStart(JsonOps.INSTANCE, structure()).getOrThrow(false, EngineersMultitool.LOGGER::error);

                pJson.add("ingredients", ingredients);
                pJson.add("structure", structure);
            }

            @Override
            public ResourceLocation getId() {
                return id();
            }

            @Override
            public RecipeSerializer<?> getType() {
                return SERIALIZER;
            }

            @Override
            public JsonObject serializeAdvancement() {
                return advancement().serializeToJson();
            }

            @Override
            public ResourceLocation getAdvancementId() {
                return advancementId();
            }
        }
    }
}
