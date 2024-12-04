package xyz.ar06.disx.entities;

import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxServerAudioPlayerRegistry;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;

public class DisxAdvancedJukeboxEntity extends BlockEntity implements ContainerSingleItem {

    private NonNullList<ItemStack> itemInventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public DisxAdvancedJukeboxEntity(BlockPos blockPos, BlockState blockState) {
        super(
                DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx", "advanced_jukebox_entity")),
                blockPos,
                blockState
        );
    }

    public boolean isHas_record() {
        return !itemInventory.get(0).equals(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, itemInventory);
        super.saveAdditional(compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        ContainerHelper.loadAllItems(compoundTag, itemInventory);
        super.load(compoundTag);
    }

    public static void registerEntity(Registrar<BlockEntityType<?>> registry){
        RegistrySupplier<BlockEntityType<?>> registration = registry.register(new ResourceLocation("disx","advanced_jukebox_entity"), () -> BlockEntityType.Builder.of(DisxAdvancedJukeboxEntity::new, DisxAdvancedJukebox.blockRegistration.get()).build(null));
    }

    @Override
    public ItemStack getItem(int i) {
        return itemInventory.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack returnStack = itemInventory.get(i).copy();
        itemInventory.set(i, ItemStack.EMPTY);
        return returnStack;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        itemInventory.set(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}