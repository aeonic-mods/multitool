package design.aeonic.multitool.registry;

import com.google.common.base.Suppliers;
import design.aeonic.multitool.api.Constants;
import design.aeonic.multitool.data.recipes.MultitoolBuildingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

public class EMRecipeTypes {
    public static final Supplier<RecipeType<MultitoolBuildingRecipe>> MULTITOOL_BUILDING = Suppliers.memoize(() -> RecipeType.register(Constants.MOD_ID + ":multitool_building"));

    public static void register() {
        MULTITOOL_BUILDING.get();
    }
}
