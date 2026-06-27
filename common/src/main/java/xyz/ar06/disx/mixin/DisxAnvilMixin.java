package xyz.ar06.disx.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.ar06.disx.items.DisxCustomDisc;

@Mixin(AnvilMenu.class)
public class DisxAnvilMixin {
    @Inject(method = "createResult", at = @At("TAIL"))
    private void modifyResult(CallbackInfo ci){
        AnvilMenu menu = (AnvilMenu)(Object) this;

        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        ItemStack output = menu.getSlot(2).getItem();

        if ((left.getItem() instanceof DisxCustomDisc)){
            if (right.getItem() instanceof EnchantedBookItem enchantedBook){
                System.out.println(right.getOrCreateTag());
                if (right.getOrCreateTag().contains("StoredEnchantments")) {
                    System.out.println("check 1");
                    CompoundTag stackTag = right.getOrCreateTag();
                    System.out.println(stackTag.getTagType("StoredEnchantments"));
                    ListTag enchantments = stackTag.getList("StoredEnchantments", Tag.TAG_COMPOUND);
                    for (Tag tag : enchantments){
                        if (tag instanceof CompoundTag compoundTag){
                            if (!((compoundTag.get("id").getAsString().equals("disx:retrograde_curse"))
                            || compoundTag.get("id").getAsString().equals("disx:tempo_curse"))){
                                menu.getSlot(2).set(ItemStack.EMPTY);
                            };
                        }
                    }
                }
            } else {
                menu.getSlot(2).set(ItemStack.EMPTY);
            }
        }
    }
}
