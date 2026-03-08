package xyz.ar06.disx.items;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class DisxCustomDisc extends Item {
    private String discType;

    public static String[] validTypes = {"blue", "diamond", "gold", "green", "iron", "orange", "pink", "purple", "red", "yellow", "default"};

    public static Item[] validDiscFactors = {
            Items.DIAMOND.asItem(),
            Items.IRON_INGOT.asItem(),
            Items.GOLD_INGOT.asItem(),
            Items.GREEN_DYE.asItem(),
            Items.BLUE_DYE.asItem(),
            Items.ORANGE_DYE.asItem(),
            Items.PINK_DYE.asItem(),
            Items.PURPLE_DYE.asItem(),
            Items.RED_DYE.asItem(),
            Items.YELLOW_DYE.asItem()
    };

    private static RegistrySupplier<Item> diamond;
    private static RegistrySupplier<Item> gold;
    private static RegistrySupplier<Item> iron;
    private static RegistrySupplier<Item> blue;
    private static RegistrySupplier<Item> green;
    private static RegistrySupplier<Item> orange;
    private static RegistrySupplier<Item> pink;
    private static RegistrySupplier<Item> purple;
    private static RegistrySupplier<Item> red;
    private static RegistrySupplier<Item> yellow;
    private static RegistrySupplier<Item> defaultt;






    public DisxCustomDisc(Properties properties, String type) {
        super(properties);
        setDiscType(type);
    }

    public void setDiscType(String type){
        this.discType = type;
    }

    public String getDiscType() {
        return discType;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null){
            String videoName = tag.getString("discName");
            if (!videoName.isEmpty() && !videoName.isBlank()){
                list.add(Component.literal(videoName).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(Component.literal("BLANK").withStyle(ChatFormatting.ITALIC));
            }
        } else {
            list.add(Component.literal("BLANK").withStyle(ChatFormatting.ITALIC));
        }
        super.appendHoverText(itemStack, level, list, tooltipFlag);
    }

    public static void registerCustomDiscs(Registrar<Item> itemRegistrar){
        diamond = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_diamond"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "diamond"));
        gold = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_gold"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "gold"));
        iron = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_iron"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "iron"));
        blue = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_blue"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "blue"));
        green = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_green"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "green"));
        orange = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_orange"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "orange"));
        pink = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_pink"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "pink"));
        purple = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_purple"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "purple"));
        red = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_red"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "red"));
        yellow = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_yellow"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "yellow"));
        defaultt = itemRegistrar.register(
                new ResourceLocation("disx","custom_disc_default"),
                () -> new DisxCustomDisc(new Item.Properties().stacksTo(1), "default"));
    }
}
