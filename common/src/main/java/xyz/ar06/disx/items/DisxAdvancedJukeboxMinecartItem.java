package xyz.ar06.disx.items;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import xyz.ar06.disx.entities.vehicle.DisxAdvancedJukeboxMinecart;

public class DisxAdvancedJukeboxMinecartItem extends Item {
    public static RegistrySupplier<Item> itemRegistration;
    public DisxAdvancedJukeboxMinecartItem(Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemStack = useOnContext.getItemInHand();
            if (!level.isClientSide) {
                RailShape railShape = blockState.getBlock() instanceof BaseRailBlock ? (RailShape)blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                double d = 0.0;
                if (railShape.isAscending()) {
                    d = 0.5;
                }
                DisxAdvancedJukeboxMinecart minecart = new DisxAdvancedJukeboxMinecart(DisxAdvancedJukeboxMinecart.entityTypeRegistration.get(), level);
                minecart.setPos((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.0625 + d, (double)blockPos.getZ() + 0.5);
                if (itemStack.hasCustomHoverName()) {
                    minecart.setCustomName(itemStack.getHoverName());
                }

                level.addFreshEntity(minecart);
                level.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), level.getBlockState(blockPos.below())));
            }

            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    public static void registerItem(Registrar<Item> registrar, RegistrySupplier<CreativeModeTab> tab){
        itemRegistration = registrar.register(
                new ResourceLocation("disx", "advanced_jukebox_minecart"),
                () -> new DisxAdvancedJukeboxMinecartItem(new Item.Properties().stacksTo(1).arch$tab(tab))
        );
    }
}
