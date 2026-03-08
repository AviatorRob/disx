package xyz.ar06.disx.entities.vehicle;


import dev.architectury.event.EventResult;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.*;
import xyz.ar06.disx.blocks.DisxAdvancedJukebox;
import xyz.ar06.disx.client_only.DisxAudioInstanceRegistry;
import xyz.ar06.disx.client_only.DisxConfigRecordS2C;
import xyz.ar06.disx.items.DisxAdvancedJukeboxMinecartItem;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.utils.DisxYTDLPWrapper;
import xyz.ar06.disx.utils.DisxYoutubeInfoScraper;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class DisxAdvancedJukeboxMinecart extends Minecart implements ContainerEntity {
    NonNullList<ItemStack> items;

    public static RegistrySupplier<EntityType<?>> entityTypeRegistration;
    public DisxAdvancedJukeboxMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Override
    public boolean hasPassenger(Entity entity) {
        return false;
    }

    private boolean isHas_Record(){
        return !this.items.get(0).equals(ItemStack.EMPTY);
    }
    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (interactionHand.equals(InteractionHand.MAIN_HAND) && !player.level().isClientSide()){
            ItemStack handStack = player.getMainHandItem();
            if (handStack.getItem() instanceof DisxCustomDisc){
                if (!isHas_Record()){
                    DisxLogger.debug("Does not have record, taking and putting in");
                    ItemStack stackCopy = handStack.copyWithCount(1);
                    handStack.setCount(handStack.getCount() - 1);
                    setItem(0, stackCopy, player);
                    CompoundTag tag = handStack.getTag();
                    String videoId = tag.getString("videoId");
                    DisxServerPacketIndex.ServerPackets.loadingVideoIdMessage(videoId, player);
                    this.level().playSound(null, this, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 1.0F);
                    CompletableFuture.runAsync(() -> this.tryGetUpdatedDiscName(player));
                } else {
                    DisxLogger.debug("Has record, taking out and putting in inventory");
                    ItemStack stack = this.items.get(0).copyWithCount(1);
                    removeItem(0, 1);
                    this.level().playSound(null, this, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.getInventory().add(stack);
                }
            } else if (handStack.isEmpty()) {
                if (isHas_Record()){
                    DisxLogger.debug("Has record, taking out and putting in inventory");
                    ItemStack stack = this.items.get(0).copyWithCount(1);
                    this.level().playSound(null, this, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    removeItem(0, 1);
                    player.getInventory().add(stack);
                }
            }
        }
        return super.interact(player, interactionHand);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return DisxAdvancedJukebox.blockRegistration.get().defaultBlockState();
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    public void setDisplayBlockState(BlockState blockState) {
        super.setDisplayBlockState(blockState);
    }

    public static void registerEntityType(Registrar<EntityType<?>> registrar){
        entityTypeRegistration = registrar.register(
                new ResourceLocation("disx","advanced_jukebox_minecart"),
                () -> EntityType.Builder.of(DisxAdvancedJukeboxMinecart::new, MobCategory.MISC).build("advanced_jukebox_minecart")
        );
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.98F, 0.7F);
    }

    @Override
    protected AABB getBoundingBoxForPose(Pose pose) {
        return new AABB(getX() - 0.49F, getY(), getZ() - 0.49F, getX() + 0.49F, getY() + 0.7F, getZ() + 0.49F);
    }

    @Override
    protected AABB makeBoundingBox() {
        return new AABB(getX() - 0.49F, getY(), getZ() - 0.49F, getX() + 0.49F, getY() + 0.7F, getZ() + 0.49F);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(DisxAdvancedJukeboxMinecartItem.itemRegistration.get(), 1);
    }

    @Override
    protected Item getDropItem() {
        return DisxAdvancedJukeboxMinecartItem.itemRegistration.get();
    }

    @Override
    public void destroy(DamageSource damageSource) {
        super.destroy(damageSource);
    }

    @Override
    public void remove(RemovalReason removalReason) {
        DisxServerAudioRegistry.removeFromRegistry(BlockPos.ZERO, this.level().dimension(), this.getUUID(), DisxAudioMotionType.LIVE);
        ItemStack stack = getItem(0);
        if (!stack.isEmpty()){
            ItemStack drop = stack.copyWithCount(1);
            ItemEntity itemEntity = new ItemEntity(level(), getX(), getY(), getZ(), drop);
            level().addFreshEntity(itemEntity);
        }
        super.remove(removalReason);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        ContainerHelper.loadAllItems(compoundTag, items);
        super.load(compoundTag);
    }

    @Override
    public boolean save(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, items);
        return super.save(compoundTag);
    }
    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public ItemStack getItem(int i) {
        return items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack stack = items.get(i).copyWithCount(1);
        items.set(0, ItemStack.EMPTY);
        if (stack.getItem() instanceof DisxCustomDisc){
            DisxServerAudioRegistry.removeFromRegistry(this.getOnPos(), this.level().dimension(), this.getUUID(), DisxAudioMotionType.LIVE);
        }
        this.setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return items.remove(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        items.set(i, itemStack);
        if (itemStack.getItem() instanceof DisxCustomDisc){
            CompoundTag tag = itemStack.getTag();
            String videoId = tag.getString("videoId");
            if (!videoId.isEmpty()){
                DisxLogger.debug("Calling add to registry (LIVE)");
                DisxServerAudioRegistry.addToRegistry(this.getOnPos(), videoId, null, this.level().dimension(), false, DisxAudioMotionType.LIVE, this.getUUID());
            }
        }

        this.setChanged();
    }

    public void setItem(int i, ItemStack itemStack, Player player) {
        items.set(i, itemStack);
        if (itemStack.getItem() instanceof DisxCustomDisc){
            CompoundTag tag = itemStack.getTag();
            String videoId = tag.getString("videoId");
            if (!videoId.isEmpty()){
                DisxLogger.debug("Calling add to registry (LIVE)");
                DisxServerAudioRegistry.addToRegistry(this.getOnPos(), videoId, player, this.level().dimension(), false, DisxAudioMotionType.LIVE, this.getUUID());
            }
        }
        this.setChanged();
    }

    public void tryGetUpdatedDiscName(Player player){
        ItemStack discStack = this.items.get(0);
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
                    if (discStack.equals(this.items.get(0))){
                        DisxLogger.debug("Disc stack still the same in Advanced Jukebox, setting updated nbt tag");
                        discStack.setTag(compoundTag);
                        player.sendSystemMessage(Component.translatable("sysmsg.disx.updated_disc_name", "Advanced Jukebox Minecart"));
                        player.sendSystemMessage(Component.translatable("sysmsg.disx.updated_disc_name.name", videoName).withStyle(ChatFormatting.GRAY));
                    }
                } else {
                    DisxLogger.debug("Video name not found once more");
                }
            }
        }
    }

        @Override
    public void setChanged() {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items);
        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        ContainerHelper.loadAllItems(compoundTag, this.items);
        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() {
        return null;
    }

    @Override
    public void setLootTable(@Nullable ResourceLocation resourceLocation) {

    }

    @Override
    public long getLootTableSeed() {
        return 0;
    }

    @Override
    public void setLootTableSeed(long l) {

    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return null;
    }

    @Override
    public void clearItemStacks() {

    }

    @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }

    @Override
    protected void applyNaturalSlowdown() {
        float f = 0.995F;

        if (this.isInWater()) {
            f *= 0.95F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0, (double)f));
    }

    private int particleTickCount = 0;
    @Override
    public void tick() {
        if (this.level().isClientSide()){
            particleTickCount++;
            if (particleTickCount == 9){
                if (DisxAudioInstanceRegistry.isNodeOnEntity(this.getUUID())&& DisxConfigRecordS2C.getSoundParticles()) {
                    BlockPos blockPos = this.getOnPos();
                    Level level = this.level();
                    float noteColor = level.random.nextInt(25) / 24.0f;
                    level.addParticle(ParticleTypes.NOTE,
                            blockPos.getX() + 0.5,
                            blockPos.getY() + 1.1,
                            blockPos.getZ() + 0.5,
                            noteColor, 0, 0);
                }
                particleTickCount = 0;
            }

        } else {
            DisxServerAudioRegistry.modifyEntryLoop(this.getUUID(), this.level().getBestNeighborSignal(this.getOnPos()) > 0);
        }
        super.tick();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        if (i == 0 && itemStack.getItem() instanceof DisxCustomDisc && !DisxServerAudioRegistry.isNodeOnEntity(this.getUUID())){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        if (i == 0){
            if (itemStack.getItem() instanceof DisxCustomDisc){
                if (!DisxServerAudioRegistry.isNodeOnEntity(this.getUUID())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pauseResumeDebounce = false;

    /*
    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (!this.level().isClientSide() && !pauseResumeDebounce) {
            boolean audioExists = DisxServerAudioRegistry.isNodeOnEntity(this.getUUID());
            boolean fromPlayer = damageSource.is(DamageTypes.PLAYER_ATTACK);
            if (audioExists && !fromPlayer){
                pauseResumeDebounce = true;
                boolean paused = DisxServerAudioRegistry.pauseOrPlayNode(this.getUUID());
                if (damageSource.getEntity() != null){
                    if (damageSource.getEntity().getType().equals(EntityType.PLAYER) && audioExists) {
                        ServerPlayer player = this.level().getServer().getPlayerList().getPlayer(damageSource.getEntity().getUUID());
                        DisxServerPacketIndex.ServerPackets.pauseMsg(player, paused);
                    }
                }
                if (paused){
                    DisxLogger.debug("Playing static sound?");
                    this.level().playSound(this, this.getOnPos(), DisxSoundEvents.SoundInstances.ADVANCED_JUKEBOX_STATIC.get(), SoundSource.RECORDS, 1.0F, 1.0F);
                }
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(6500);
                        pauseResumeDebounce = false;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return super.hurt(damageSource, f);
    }
     */

    @Override
    public boolean isEmpty() {
        return this.items.get(0).isEmpty();
    }

    @Override
    public boolean isChestVehicleEmpty() {
        return isEmpty();
    }
}
