package xyz.ar06.disx.entities;

import net.minecraft.network.chat.Component;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.utils.DisxInternetCheck;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.utils.DisxYoutubeInfoScraper;
import xyz.ar06.disx.utils.DisxYoutubeTitleScraper;
import xyz.ar06.disx.blocks.DisxLacquerBlock;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.items.DisxRecordStamp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


public class DisxStampMakerEntity extends BlockEntity implements Container, WorldlyContainer {

    private NonNullList<ItemStack> items;

    private String videoId;


    public DisxStampMakerEntity(BlockPos blockPos, BlockState blockState) {
        super(
                DisxMain.REGISTRAR_MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE).get(new ResourceLocation("disx","stamp_maker_entity")),
                blockPos,
                blockState);
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        DisxMain.LOGGER.info("MAKING NEW ENTITY AT " + blockPos);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        boolean returnValue = true;
        for (ItemStack stack : this.items){
            if (!stack.equals(ItemStack.EMPTY)){
                returnValue = false;
            }
        }
        return returnValue;
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack stack = this.items.get(i).copy();
        this.items.set(i, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack returnStack = this.items.get(i).copy();
        this.items.set(i, ItemStack.EMPTY);
        return returnStack;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (i == 1){
            String videoId = itemStack.getHoverName().getString();
            this.videoId = videoId;
            DisxLogger.debug("set video id to " + videoId);
            DisxLogger.debug("to confirm: " + this.videoId);
        } else {
            this.items.set(i, itemStack);
        }
        checkTryAsyncProductionCond();
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items);
        super.load(compoundTag);
    }

    public void setItem(int i, ItemStack itemStack, Player player) {;
        this.items.set(i, itemStack);
        if (itemStack.getItem().equals(DisxLacquerBlock.itemRegistration.get())){
            if (!this.isVideoIdNull()){
                produceStamp(player);
            }
        }
    }

    public void setItems(NonNullList<ItemStack> items) {
        this.items = items;
        checkTryAsyncProductionCond();
    }


    public void checkTryAsyncProductionCond(){
        DisxLogger.debug("checking async production conditions");
        /*if (!this.items.get(1).equals(ItemStack.EMPTY)){
            String videoId = this.items.get(1).getHoverName().getString();
            this.videoId = videoId;
        }*/
        if (this.items.get(0).getItem().equals(DisxLacquerBlock.itemRegistration.get())){
            DisxLogger.debug("lacquer block found");
            if (!this.isVideoIdNull()){
                DisxLogger.debug("video id is not null, running produce async");
                produceStampAsync();
            }
        }
    }

    @Override
    public void setChanged() {
        this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 1);
        DisxLogger.debug("should've sent block update");
        super.setChanged();
    }



    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items);
        super.saveAdditional(compoundTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithFullMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setVideoId(String videoId, Player player) {
        if (videoId.equals("") || videoId.equals(" ")){
            this.videoId = null;
        } else {
            this.videoId = videoId;
            if (this.getItem(0).is(DisxLacquerBlock.itemRegistration.get())){
                produceStamp(player);
                this.setChanged();
            }
        }
    }

    public String getVideoId() {
        return videoId;
    }

    public Boolean isVideoIdNull(){
        if (videoId == null){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    public void produceStamp(Player player){
        if (!DisxInternetCheck.checkInternet()){
            DisxSystemMessages.noInternetErrorMessage(player);
        } else {
            ArrayList<String> title_and_length = DisxYoutubeInfoScraper.scrapeLengthAndTitle(this.videoId);
            String videoName = title_and_length.get(0);
            if (videoName.equals("Video Not Found") && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")){
                DisxSystemMessages.noVideoFound(player);
                return;
            }
            int videoLength = Integer.valueOf(title_and_length.get(1));
            if (videoLength > 1800) {
                DisxSystemMessages.badDuration(player);
                return;
            }
            this.setItem(0, ItemStack.EMPTY);
            BlockPos blockPos = this.getBlockPos();
            Level lvl = getLevel();
            lvl.playSound(null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS);
            DisxRecordStamp stamp = (DisxRecordStamp) DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx", "record_stamp"));
            CompoundTag tag = new CompoundTag();
            tag.putString("videoId", this.videoId);
            tag.putString("videoName", videoName);
            ItemStack stampStack = new ItemStack(stamp);
            stampStack.setTag(tag);
            stampStack.setCount(1);
            if (lvl.getBlockEntity(blockPos.below()) != null && lvl.getBlockEntity(blockPos.below()) instanceof HopperBlockEntity){
                this.items.set(2, stampStack);
                this.items.set(1, ItemStack.EMPTY);
                this.videoId = null;
                lvl.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS);
            } else {
                ItemEntity itemEntity = new ItemEntity(getLevel(), ((double) blockPos.getX()) + 0.5, ((double) blockPos.getY()) + 0.2, ((double) blockPos.getZ()) + 0.5, stampStack);
                itemEntity.setDefaultPickUpDelay();
                this.items.set(1, ItemStack.EMPTY);
                this.videoId = null;
                lvl.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    public void produceStampAsync(){
        if (!DisxInternetCheck.checkInternet()){
                DisxSystemMessages.noInternetFoundStampMakerAsync(this.getLevel().getServer(), this.getBlockPos());
        } else {
            ArrayList<String> title_and_length = DisxYoutubeInfoScraper.scrapeLengthAndTitle(this.videoId);
            String videoName = title_and_length.get(0);
            if (videoName.equals("Video Not Found") && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")){
                DisxSystemMessages.videoNotFoundStampMakerAsync(this.getLevel().getServer(), this.getBlockPos());
                return;
            }
            int videoLength = Integer.valueOf(title_and_length.get(1));
            if (videoLength > 1800) {
                DisxSystemMessages.badDurationStampMakerAsync(this.getLevel().getServer(), this.getBlockPos());
                return;
            }
            this.setItem(0, ItemStack.EMPTY);
            BlockPos blockPos = this.getBlockPos();
            Level lvl = getLevel();
            lvl.playSound(null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS);
            DisxRecordStamp stamp = (DisxRecordStamp) DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM).get(new ResourceLocation("disx", "record_stamp"));
            CompoundTag tag = new CompoundTag();
            tag.putString("videoId", this.videoId);
            tag.putString("videoName", videoName);
            ItemStack stampStack = new ItemStack(stamp);
            stampStack.setTag(tag);
            stampStack.setCount(1);
            if (lvl.getBlockEntity(blockPos.below()) != null && lvl.getBlockEntity(blockPos.below()) instanceof HopperBlockEntity){
                this.items.set(2, stampStack);
                this.items.set(1, ItemStack.EMPTY);
                this.videoId = null;
                lvl.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS);
            } else {
                ItemEntity itemEntity = new ItemEntity(getLevel(), ((double) blockPos.getX()) + 0.5, ((double) blockPos.getY()) + 0.2, ((double) blockPos.getZ()) + 0.5, stampStack);
                itemEntity.setDefaultPickUpDelay();
                lvl.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS);
                level.addFreshEntity(itemEntity);
                this.items.set(1, ItemStack.EMPTY);
                this.videoId = null;
            }
        }
    }


    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction.equals(Direction.UP)){
            int[] returnValue = new int[]{1};
            return returnValue;
        }
        ArrayList<Object> sideDirections = new ArrayList<>();
        sideDirections.add(Direction.NORTH);
        sideDirections.add(Direction.EAST);
        sideDirections.add(Direction.SOUTH);
        sideDirections.add(Direction.WEST);
        for (Object o : sideDirections){
            if (direction.equals(o)){
                int[] returnValue = new int[]{0};
                return returnValue;
            }
        }
        if (direction.equals(Direction.DOWN)){
            int[] returnValue = new int[]{2};
            return returnValue;
        }
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        ArrayList<Object> sideDirections = new ArrayList<>();
        sideDirections.add(Direction.NORTH);
        sideDirections.add(Direction.EAST);
        sideDirections.add(Direction.SOUTH);
        sideDirections.add(Direction.WEST);
        for (Object o : sideDirections){
            if (direction.equals(o)){
                if (itemStack.getItem().equals(DisxLacquerBlock.itemRegistration.get())){
                    if (this.items.get(0).equals(ItemStack.EMPTY)){
                        return true;
                    }
                }
            }
        }
        if (direction.equals(Direction.UP)){
            if (itemStack.getItem().equals(Items.PAPER) && itemStack.hasCustomHoverName()){
                if (this.items.get(1).equals(ItemStack.EMPTY)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        if (direction.equals(Direction.DOWN) && itemStack.getItem().equals(DisxRecordStamp.getItemRegistration().get())){
            return true;
        }
        return false;
    }

    @Override
    public void clearContent() {
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
    }
}
