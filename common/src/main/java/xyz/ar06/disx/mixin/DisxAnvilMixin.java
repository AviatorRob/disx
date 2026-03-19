package xyz.ar06.disx.mixin;

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

        if (!(right.getItem() instanceof EnchantedBookItem)) return;

        if (!(left.getItem() instanceof DisxCustomDisc)){
            menu.getSlot(2).set(ItemStack.EMPTY);
        }
    }
}
