package xyz.ar06.disx.blocks;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.ar06.disx.*;
import xyz.ar06.disx.audio_filters.DisxAudioFilterType;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.utils.*;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class DisxEnderAdvancedJukebox extends Block{
    protected DisxEnderAdvancedJukebox(Properties properties) {
        super(properties);
    }

    public static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> blockItemRegistration;

    private boolean debounce = false;

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide() && interactionHand == InteractionHand.MAIN_HAND && !debounce && !DisxJukeboxUsageCooldownManager.isOnCooldown(blockPos, level.dimension())){
            ItemStack handStack = player.getMainHandItem();
            if (handStack.getItem() instanceof DisxCustomDisc){
                DisxLogger.debug("Player interacted with block with custom disc in hand");
                if (player instanceof DisxEnderAdvancedJukeboxInventoryHelper helper){
                    DisxLogger.debug("Mixin was registered successfully at runtime;");
                    debounce = true;
                    CompoundTag invTag = helper.disx$getEnderAdvancedJukeboxInventory();
                    NonNullList<ItemStack> invList = NonNullList.withSize(1, ItemStack.EMPTY);
                    if (!invTag.isEmpty()){
                        ContainerHelper.loadAllItems(invTag, invList);
                    }
                    if (invList.get(0).isEmpty()){
                        CompoundTag handStackTag = handStack.getOrCreateTag();
                        if (handStackTag != null && handStackTag.get("videoId") != null && !handStackTag.getString("videoId").isEmpty()){
                            DisxLogger.debug("doesn't have record, putting it in");
                            if (!isPassingAudioPrerequisiteChecks(player, level)){
                                DisxLogger.debug("player did not pass audio prerequisite check");
                                return InteractionResult.FAIL;
                            }
                            String discName = handStackTag.getString("discName");
                            String videoId = handStackTag.getString("videoId");
                            //alpha discs compatibility
                            if (discName.isBlank() || discName.isEmpty()){
                                handStackTag.putString("discName", handStack.getHoverName().getString());
                                handStack.setTag(handStackTag);
                            }
                            invList.set(0, handStack.copyWithCount(1));
                            DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                            DisxLogger.debug("Sending loading video message");
                            DisxServerPacketIndex.ServerPackets.loadingVideoIdMessage(videoId, player);
                            DisxLogger.debug("Calling add to registry (LIVE)");
                            DisxServerAudioRegistry.addToRegistry(blockPos, videoId, player, level.dimension(), false, DisxAudioMotionType.LIVE, player.getUUID(), -1, new ArrayList<DisxAudioFilterType>());
                            helper.disx$setEnderAdvancedJukeboxInventory(ContainerHelper.saveAllItems(new CompoundTag(), invList));
                            handStack.shrink(1);
                            level.playSound(null, blockPos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.0F);
                            CompletableFuture.runAsync(() -> tryGetUpdatedDiscName(player));
                        }
                    } else {
                        DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                        DisxLogger.debug("Detected record, returning it and killing audio");
                        ItemStack returnStack = invList.get(0).copyWithCount(1);
                        invList.set(0, ItemStack.EMPTY);
                        DisxServerAudioRegistry.removeFromRegistry(blockPos, level.dimension(), player.getUUID(), DisxAudioMotionType.LIVE);
                        player.addItem(returnStack);
                        helper.disx$setEnderAdvancedJukeboxInventory(ContainerHelper.saveAllItems(new CompoundTag(), invList));
                        level.playSound(null, blockPos, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    debounce = false;
                    return InteractionResult.SUCCESS;
                }
            } else if (handStack.isEmpty()){
                DisxLogger.debug("Player interacted with block with empty hand");
                if (player instanceof DisxEnderAdvancedJukeboxInventoryHelper helper){
                    DisxLogger.debug("Mixin was registered successfully at runtime;");
                    debounce = true;
                    CompoundTag invTag = helper.disx$getEnderAdvancedJukeboxInventory();
                    NonNullList<ItemStack> invList = NonNullList.withSize(1, ItemStack.EMPTY);
                    if (!invTag.isEmpty()){
                        ContainerHelper.loadAllItems(invTag, invList);
                    }
                    if (!invList.get(0).isEmpty()){
                        DisxJukeboxUsageCooldownManager.updateCooldown(blockPos, level.dimension());
                        DisxLogger.debug("Detected record, returning it and killing audio");
                        ItemStack returnStack = invList.get(0).copyWithCount(1);
                        invList.set(0, ItemStack.EMPTY);
                        DisxServerAudioRegistry.removeFromRegistry(blockPos, level.dimension(), player.getUUID(), DisxAudioMotionType.LIVE);
                        player.addItem(returnStack);
                        helper.disx$setEnderAdvancedJukeboxInventory(ContainerHelper.saveAllItems(new CompoundTag(), invList));
                        level.playSound(null, blockPos, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    debounce = false;
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.SUCCESS;

        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    public void tryGetUpdatedDiscName(Player player){
        NonNullList<ItemStack> itemInventory = NonNullList.withSize(1, ItemStack.EMPTY);
        if (player instanceof DisxEnderAdvancedJukeboxInventoryHelper helper){
            ContainerHelper.loadAllItems(helper.disx$getEnderAdvancedJukeboxInventory(), itemInventory);
            ItemStack discStack = itemInventory.get(0);
            if (!discStack.isEmpty()){
                CompoundTag compoundTag = discStack.getTag();
                DisxLogger.debug("Found disc in jukebox, checking name");
                String discName = compoundTag.getString("discName");
                String videoId = compoundTag.getString("videoId");
                if (discName.equals("Video Not Found")){
                    DisxLogger.debug("Disc has no name. Attempting to find one...");
                    String videoName = DisxYoutubeResolver.scrapeTitle(videoId);
                    if (!videoName.equals("Video Not Found")){
                        DisxLogger.debug("Found updated name: " + videoName);
                        compoundTag.putString("discName", videoName);
                        itemInventory = NonNullList.withSize(1, ItemStack.EMPTY);
                        ContainerHelper.loadAllItems(helper.disx$getEnderAdvancedJukeboxInventory(), itemInventory);
                        if (discStack.equals(itemInventory.get(0))){
                            DisxLogger.debug("Disc stack still the same in Ender Advanced Jukebox, setting updated nbt tag");
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

    public static void registerBlock(Registrar<Block> registry){
        blockRegistration = registry.register(new ResourceLocation("disx", "ender_advanced_jukebox"), () -> new DisxEnderAdvancedJukebox(BlockBehaviour.Properties.copy(Blocks.ENDER_CHEST).strength(22.5F, 600.0F)));
    }

    public static void registerBlockItem(Registrar<Item> registry, RegistrySupplier<CreativeModeTab> tab){
        blockItemRegistration = registry.register(new ResourceLocation("disx","ender_advanced_jukebox"), () -> new BlockItem(blockRegistration.get(), new Item.Properties().arch$tab(tab)));
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return false;
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        for(int i = 0; i < 3; ++i) {
            int j = randomSource.nextInt(2) * 2 - 1;
            int k = randomSource.nextInt(2) * 2 - 1;
            double d = (double)blockPos.getX() + (double)0.5F + (double)0.25F * (double)j;
            double e = (double)((float)blockPos.getY() + randomSource.nextFloat());
            double f = (double)blockPos.getZ() + (double)0.5F + (double)0.25F * (double)k;
            double g = (double)(randomSource.nextFloat() * (float)j);
            double h = ((double)randomSource.nextFloat() - (double)0.5F) * (double)0.125F;
            double l = (double)(randomSource.nextFloat() * (float)k);
            level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l);
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
}
