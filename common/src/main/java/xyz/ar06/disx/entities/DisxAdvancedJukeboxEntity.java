package xyz.ar06.disx.entities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.WorldlyContainer;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.*;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.utils.DisxYTDLPWrapper;
import xyz.ar06.disx.utils.DisxYoutubeInfoScraper;

import java.util.UUID;

public class DisxAdvancedJukeboxEntity extends BlockEntity implements ContainerSingleItem, WorldlyContainer {

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

    public boolean isRecordPlaying(){
        if (this.isHas_record()){
            return DisxServerAudioRegistry.isNodeAtLocation(this.getBlockPos(), this.getLevel().dimension());
        }
        return false;
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
        if (returnStack.getItem() instanceof DisxCustomDisc){
            DisxServerAudioRegistry.removeFromRegistry(this.getBlockPos(), this.getLevel().dimension(), new UUID(0L, 0L), DisxAudioMotionType.STATIC);

        }
        return returnStack;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (i == 0 && itemStack.getItem() instanceof DisxCustomDisc){
            itemInventory.set(i, itemStack);
            String videoId = itemStack.getTag().getString("videoId");
            int jukeboxPower = this.getLevel().getBestNeighborSignal(this.getBlockPos());
            boolean loop = jukeboxPower > 0;
            DisxServerAudioRegistry.addToRegistry(this.getBlockPos(), videoId, null, level.dimension(), loop, DisxAudioMotionType.STATIC, new UUID(0L, 0L));
        } else {
            itemInventory.set(i, itemStack);
        }

    }

    public void setItem(int i, ItemStack itemStack, Player player) {
        if (i == 0 && itemStack.getItem() instanceof DisxCustomDisc){
            itemInventory.set(i, itemStack);
            String videoId = itemStack.getTag().getString("videoId");
            int jukeboxPower = this.getLevel().getBestNeighborSignal(this.getBlockPos());
            boolean loop = jukeboxPower > 0;
            DisxServerAudioRegistry.addToRegistry(this.getBlockPos(), videoId, player, level.dimension(), loop, DisxAudioMotionType.STATIC, new UUID(0L, 0L));
        } else {
            itemInventory.set(i, itemStack);
        }

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    public void tryGetUpdatedDiscName(Player player){
        ItemStack discStack = this.itemInventory.get(0);
        if (!discStack.isEmpty()){
            CompoundTag compoundTag = discStack.getTag();
            DisxLogger.debug("Found disc in jukebox, checking name");
            String discName = compoundTag.getString("discName");
            String videoId = compoundTag.getString("videoId");
            if (discName.equals("Video Not Found")){
                DisxLogger.debug("Disc has no name. Attempting to find one...");
                String videoName = DisxYTDLPWrapper.getVideoName(videoId);
                if (!videoName.equals("Video Not Found")){
                    DisxLogger.debug("Found updated name: " + videoName);
                    compoundTag.putString("discName", videoName);
                    if (discStack.equals(this.itemInventory.get(0))){
                        DisxLogger.debug("Disc stack still the same in Advanced Jukebox, setting updated nbt tag");
                        discStack.setTag(compoundTag);
                        player.sendSystemMessage(Component.translatable("sysmsg.disx.updated_disc_name", "Advanced Jukebox"));
                        player.sendSystemMessage(Component.translatable("sysmsg.disx.updated_disc_name.name", videoName).withStyle(ChatFormatting.GRAY));
                    }
                } else {
                    DisxLogger.debug("Video name not found once more");
                }
            }
        }

    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction.equals(Direction.UP)){
            return new int[]{0};
        }
        if (direction.equals(Direction.DOWN)){
            return new int[]{0};
        }
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        if (itemStack.getItem() instanceof DisxCustomDisc && direction.equals(Direction.UP)){
            return true;
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        if (direction.equals(Direction.DOWN) && !this.isRecordPlaying()){
            return true;
        }
        return false;
    }

    @Override
    public BlockState getBlockState() {
        return super.getBlockState();
    }


}
