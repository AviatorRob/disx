package xyz.ar06.disx.client_only;

import xyz.ar06.disx.blocks.DisxRecordPress;
import xyz.ar06.disx.blocks.DisxStampMaker;
import xyz.ar06.disx.recipe_types.DisxCustomDiscRecipe;
import xyz.ar06.disx.recipe_types.DisxStampRecipe;
import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.common.util.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class DisxCompatWithREI implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new REIDisplayCategoryRecordPress());
        registry.addWorkstations(CategoryIdentifier.of("disx","plugin/record_press"), EntryStacks.of(DisxRecordPress.blockItemRegistration.get()));
        registry.add(new REIDisplayCategoryStampMaker());
        registry.addWorkstations(CategoryIdentifier.of(new ResourceLocation("disx","plugin/stamp_maker")), EntryStacks.of(DisxStampMaker.itemRegistration.get()));
        REIClientPlugin.super.registerCategories(registry);
    }



    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(DisxCustomDiscRecipe.class, REIDisplayRecordPress::new);
        registry.registerFiller(DisxStampRecipe.class, REIDisplayStampMaker::new);
        REIClientPlugin.super.registerDisplays(registry);
    }

    @Override
    public void registerEntries(EntryRegistry registry) {

        REIClientPlugin.super.registerEntries(registry);
    }

    public class REIDisplayCategoryStampMaker implements DisplayCategory{

        @Override
        public CategoryIdentifier getCategoryIdentifier() {
            return CategoryIdentifier.of("disx:plugin/stamp_maker");
        }

        @Override
        public Component getTitle() {
            return Component.literal("Stamp Maker");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(DisxStampMaker.itemRegistration.get());
        }

        @Override
        public List<Widget> setupDisplay(Display display, Rectangle bounds) {
            NonNullList<Widget> widgets = NonNullList.create();;
            Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);

            widgets.add(Widgets.createRecipeBase(bounds));

            widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));

            widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 5)));
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5))
                    .entries(display.getOutputEntries().get(0)) // Get the first output ingredient
                    .disableBackground() // Disable the background because we have our bigger background
                    .markOutput()); // Mark this as the output for REI to identify

            widgets.add(Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5))
                    .entries(display.getInputEntries().get(0)) // Get the first input ingredient
                    .markInput()); // Mark this as the input for REI to identify

            return widgets;
        }
    }
    public class REIDisplayStampMaker extends BasicDisplay{

        public REIDisplayStampMaker(DisxStampRecipe recipe) {
            this(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(recipe.getResultItem())));
        }
        public REIDisplayStampMaker(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
            super(inputs, outputs);
        }

        public REIDisplayStampMaker(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location) {
            super(inputs, outputs, location);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return CategoryIdentifier.of("disx:plugin/stamp_maker");
        }
    }

    public class REIDisplayRecordPress extends BasicDisplay {
        public REIDisplayRecordPress(DisxCustomDiscRecipe recipe) {
            this(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(recipe.getResultItem())));
        }
        public REIDisplayRecordPress(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
            super(inputs, outputs);
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return CategoryIdentifier.of("disx:plugin/record_press");
        }
    }

    public class REIDisplayCategoryRecordPress implements DisplayCategory {

        @Override
        public CategoryIdentifier getCategoryIdentifier() {
            return CategoryIdentifier.of("disx:plugin/record_press");
        }

        @Override
        public Component getTitle() {
            return Component.literal("Record Press");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(DisxRecordPress.blockItemRegistration.get());
        }

        @Override
        public List<Widget> setupDisplay(Display display, Rectangle bounds) {
            Point startPoint = new Point(bounds.getCenterX() - 60, bounds.getCenterY() - 13);
            List<Widget> widgets = new ArrayList<>();

            widgets.add(Widgets.createRecipeBase(bounds));

            widgets.add(Widgets.createArrow(new Point(startPoint.x + 61, startPoint.y + 4)));

            widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 94, startPoint.y + 5)));
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 94, startPoint.y + 5))
                    .entries(display.getOutputEntries().get(0)) // Get the first output ingredient
                    .disableBackground() // Disable the background because we have our bigger background
                    .markOutput()); // Mark this as the output for REI to identify

            widgets.add(Widgets.createSlot(new Point(startPoint.x + 40, startPoint.y + 5))
                    .entries(display.getInputEntries().get(2)) // Get the first input ingredient
                    .markInput()); // Mark this as the input for REI to identify
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 20, startPoint.y + 5))
                            .entries(display.getInputEntries().get(1))
                            .markInput());
            widgets.add(Widgets.createSlot(new Point(startPoint.x, startPoint.y + 5))
                    .entries(display.getInputEntries().get(0))
                    .markInput());

            return widgets;

        }
    }
}
