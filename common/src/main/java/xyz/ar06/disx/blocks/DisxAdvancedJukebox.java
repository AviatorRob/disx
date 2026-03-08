package xyz.ar06.disx.blocks;


import dev.architectury.event.EventResult;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.*;
import xyz.ar06.disx.DisxSoundEvents;
import xyz.ar06.disx.*;
import xyz.ar06.disx.client_only.DisxAudioInstanceRegistry;
import xyz.ar06.disx.client_only.DisxConfigRecordS2C;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.entities.DisxAdvancedJukeboxEntity;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.utils.DisxInternetCheck;
import xyz.ar06.disx.utils.DisxJukeboxUsageCooldownManager;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;


public class DisxAdvancedJukebox extends BaseEntityBlock {

    public static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> blockItemRegistration;

    protected DisxAdvancedJukebox(Properties properties) {
        super(properties);
    }

    private boolean debounce = false;
    private int power;

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DisxAdvancedJukeboxEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);

        int p = level.getBestNeighborSignal(blockPos);
        if (this.power != p){
            this.power = p;
            boolean loop = p > 0;
            DisxServerAudioRegistry.modifyEntryLoop(blockPos, level.dimension(), loop);
        }

    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {

        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND && !debounce && !DisxJukeboxUsageCooldownManager.isOnCooldown(blockPos, level.dimension())){
            ItemStack stack = player.getMainHandItem();
            Item item = null;
            if (stack != null){
                item = stack.getItem();
            }
            DisxAdvancedJukeboxEntity entity = (DisxAdvancedJukeboxEntity) level.getBlockEntity(blockPos);
            if (entity.isHas_record()){
                debounce = true;
                DisxLogger.debug("does have record, taking it out");
                DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                ItemStack stackCopy = entity.getItem(0).copyWithCount(1);
                entity.removeItem(0, 1);
                player.getInventory().add(stackCopy);
                entity.setChanged();
                DisxLogger.debug("[advanced jukebox] current has record value: " + entity.isHas_record());
                level.playSound(null, blockPos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
                debounce = false;
                return InteractionResult.SUCCESS;
            } else if (!entity.isHas_record()){
                if (item instanceof DisxCustomDisc){
                    if (stack.getTag() != null && stack.getTag().get("videoId") != null && !stack.getTag().getString("videoId").isEmpty()){
                        if (!isPassingAudioPrerequisiteChecks(player, level)){
                            return InteractionResult.FAIL;
                        }
                        DisxLogger.debug("[advanced jukebox] doesn't have record, putting it in");
                        debounce = true;
                        String discName = stack.getTag().getString("discName");
                        String videoId = stack.getTag().getString("videoId");
                        //alpha discs compatibility
                        if (discName.isBlank() || discName.isEmpty()){
                            CompoundTag updatedTag = stack.getTag();
                            updatedTag.putString("discName", stack.getHoverName().getString());
                            stack.setTag(updatedTag);
                        }
                        //player.getCooldowns().addCooldown(item, 999999999);
                        ItemStack stackCopy = stack.copy();
                        stack.setCount(stack.getCount() - 1);
                        entity.setItem(0, stackCopy, player);
                        DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                        DisxServerPacketIndex.ServerPackets.loadingVideoIdMessage(videoId, player);
                        DisxLogger.debug("[advanced jukebox] current has record value: " + entity.isHas_record());
                        level.playSound(null, blockPos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 1.0F);
                        debounce = false;
                        //player.getCooldowns().removeCooldown(item);
                        entity.setChanged();
                        //attempt to get the latest video title if disc was made with no proper title
                        CompletableFuture.runAsync(() -> entity.tryGetUpdatedDiscName(player));
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        onBlockDestroy(level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    private void onBlockDestroy(Level level, BlockPos blockPos){
        if (!level.isClientSide()) {
            DisxAdvancedJukeboxEntity entity = (DisxAdvancedJukeboxEntity) level.getBlockEntity(blockPos);
            if (entity == null) {
                DisxLogger.debug("[advanced jukebox] block destroy initialized, no block entity found");
            } else {
                if (entity.isHas_record()) {
                    DisxLogger.debug("[advanced jukebox] block destroy initialized, has record; removing record");
                    ItemStack newItemStack = entity.removeItem(0, 1);
                    ItemEntity itemEntity = new ItemEntity(entity.getLevel(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), newItemStack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        double x = blockPos.getX();
        double y = blockPos.getY();
        double z = blockPos.getZ();
        x += 0.5;
        y += 0.5;
        z += 0.5;
        ItemStack item2 = new ItemStack(blockItemRegistration.get());
        item2.setCount(1);
        ItemEntity itemEntity2 = new ItemEntity(serverLevel, x, y, z, item2);
        serverLevel.addFreshEntity(itemEntity2);
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
    }

    private static boolean isPassingAudioPrerequisiteChecks(Player player, Level level){
        // check Internet
        if (!DisxInternetCheck.checkInternet()){
            DisxSystemMessages.noInternetErrorMessage(player);
            return false;
        }
        //Check Use-Blacklist, Use-Whitelist, and Dimension-Blacklist
        if (DisxConfigHandler.SERVER.isOnUseBlacklist(player.getUUID())){
            DisxSystemMessages.blacklistedByServer(player);
            return false;
        }
        if (!DisxConfigHandler.SERVER.isOnUseWhitelist(player.getUUID())){
            DisxSystemMessages.notWhitelistedByServer(player);
            return false;
        }
        if (DisxConfigHandler.SERVER.isOnDimensionBlacklist(level.dimension())){
            DisxSystemMessages.dimensionBlacklisted(player);
            return false;
        }
        //Check Audio Player Count
        int maxAudioPlayerCt = Integer.valueOf(DisxConfigHandler.SERVER.getProperty("max_audio_players"));
        int currentAudioPlayerCt = DisxServerAudioRegistry.getRegistryCount();
        if (currentAudioPlayerCt >= maxAudioPlayerCt){
            DisxSystemMessages.maxAudioPlayerCtReached(player);
            return false;
        }
        return true;
    }

    /*
    @Override
    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        onBlockDestroy(levelAccessor, blockPos);
        super.destroy(levelAccessor, blockPos, blockState);
    }
     */

    /*
    @Override
    @PlatformOnly("fabric")
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        onBlockDestroy(level, blockPos);
        super.playerWillDestroy(level, blockPos, blockState, player);
    }
    */
    public static void registerBlock(Registrar<Block> registry){
        blockRegistration = registry.register(new ResourceLocation("disx", "advanced_jukebox"), () -> new DisxAdvancedJukebox(BlockBehaviour.Properties.copy(Blocks.JUKEBOX)));
    }

    public static void registerBlockItem(Registrar<Item> registry, RegistrySupplier<CreativeModeTab> tab){
        blockItemRegistration = registry.register(new ResourceLocation("disx","advanced_jukebox"), () -> new BlockItem(blockRegistration.get(), new Item.Properties().arch$tab(tab)));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (((DisxAdvancedJukeboxEntity) level.getBlockEntity(blockPos)).isRecordPlaying()){
            DisxLogger.debug("Comparator Reading: yes there is a record playing");
            return 16;
        }
        DisxLogger.debug("Comparator Reading: no there is not a record playing");
        return 0;
    }

    public static EventResult leverListener(Player player, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
        Level level = player.level();
        if (!player.level().isClientSide() && interactionHand.equals(InteractionHand.MAIN_HAND)){
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof LeverBlock){
                Direction facingDirection = blockState.getValue(LeverBlock.FACING).getOpposite();
                BlockState adjacentBlockState = level.getBlockState(blockPos.relative(facingDirection));
                if (adjacentBlockState.is(DisxAdvancedJukebox.blockRegistration.get())){
                    DisxServerPacketIndex.ServerPackets.loopMsg(player, !blockState.getValue(LeverBlock.POWERED));
                }
            }
        }

        return EventResult.pass();
    }

    private boolean pauseResumeDebounce = false;
    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {

        if (!level.isClientSide() && !pauseResumeDebounce) {
            boolean audioExists = DisxServerAudioRegistry.isNodeAtLocation(blockHitResult.getBlockPos(), level.dimension());
            if (!audioExists){
                return;
            }
            pauseResumeDebounce = true;
            boolean paused = DisxServerAudioRegistry.pauseOrPlayNode(blockHitResult.getBlockPos(), level.dimension());
            if (projectile.getOwner() != null) {
                if (projectile.getOwner().getType().equals(EntityType.PLAYER) && audioExists){
                    ServerPlayer player = level.getServer().getPlayerList().getPlayer(projectile.getOwner().getUUID());
                    DisxServerPacketIndex.ServerPackets.pauseMsg(player, paused);
                }
            }
            if (paused){
                DisxLogger.debug("Playing static sound?");
                level.playSound(null, blockHitResult.getBlockPos(), DisxSoundEvents.SoundInstances.ADVANCED_JUKEBOX_STATIC.get(), SoundSource.RECORDS, 1.0f, 1.0f);
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

        super.onProjectileHit(level, blockState, blockHitResult, projectile);
    }

    private int particleTickCount = 0;
    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (level.isClientSide()){
            particleTickCount++;
            if (particleTickCount == 9){
                if (DisxAudioInstanceRegistry.isNodeAtLocation(blockPos, level.dimension().location()) && DisxConfigRecordS2C.getSoundParticles()){
                    float noteColor = level.random.nextInt(25) / 24.0f;
                    level.addParticle(ParticleTypes.NOTE,
                            blockPos.getX() + 0.5,
                            blockPos.getY() + 1.1,
                            blockPos.getZ() + 0.5,
                            noteColor, 0, 0);
                }
                particleTickCount = 0;
            }
        }
        super.animateTick(blockState, level, blockPos, randomSource);
    }
}
