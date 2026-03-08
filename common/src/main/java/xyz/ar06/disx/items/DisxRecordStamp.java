package xyz.ar06.disx.items;

import xyz.ar06.disx.DisxMain;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.registry.client.gui.ClientTooltipComponentRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisxRecordStamp extends Item {
    private static RegistrySupplier<Item> itemRegistration;

    static Properties properties = new Item.Properties()
            .stacksTo(1)
            .durability(3)
            .defaultDurability(3);
    public DisxRecordStamp() {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null){
            String videoName = tag.getString("videoName");
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

    public static void registerItem(Registrar<Item> registrar){
        itemRegistration = registrar.register(
                new ResourceLocation("disx","record_stamp"),
                () -> new DisxRecordStamp()
        );
    }

    public static RegistrySupplier<Item> getItemRegistration() {
        return itemRegistration;
    }
}
