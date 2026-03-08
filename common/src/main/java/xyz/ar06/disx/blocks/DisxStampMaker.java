package xyz.ar06.disx.blocks;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.DisxServerPacketIndex;
import xyz.ar06.disx.entities.DisxStampMakerEntity;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static xyz.ar06.disx.DisxMain.REGISTRAR_MANAGER;
import static xyz.ar06.disx.blocks.DisxStampMakerShapes.*;

public class DisxStampMaker extends BaseEntityBlock {

    public static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> itemRegistration;
    public static RegistrySupplier<BlockEntityType> blockEntityRegistration;

    public static Logger logger = LoggerFactory.getLogger("disx");

    //DisxStampMakerEntity blockEntity;

    static DirectionProperty facingProperty = DirectionProperty.create("facing");

    static VoxelShape blockShape0 = makeShape0();
    static VoxelShape blockShape90 = makeShape90();
    static VoxelShape blockShape180 = makeShape180();
    static VoxelShape blockShape270 = makeShape270();

    public String videoId;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facingProperty);
        super.createBlockStateDefinition(builder);
    }

    protected DisxStampMaker(Properties properties) {
        super(properties);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DisxStampMakerEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public static VoxelShape getCorrespondingShape(Direction direction){
        if (direction.equals(Direction.SOUTH)){
            return blockShape90;
        } else if (direction.equals(Direction.EAST)){
            return blockShape180;
        } else if (direction.equals(Direction.NORTH)){
            return blockShape270;
        } else {
            return blockShape0;
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction direction = blockState.getValue(facingProperty);
        return getCorrespondingShape(direction);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction direction = blockState.getValue(facingProperty);
        return getCorrespondingShape(direction);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState state = this.stateDefinition.any().setValue(facingProperty, blockPlaceContext.getHorizontalDirection());
        return state;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (interactionHand == InteractionHand.MAIN_HAND){
            DisxLogger.debug("Interaction hand was main hand");
            ItemStack itemStack = player.getMainHandItem();
            DisxStampMakerEntity blockEntity = (DisxStampMakerEntity) level.getBlockEntity(blockPos);
            if (itemStack.getItem().equals(DisxLacquerBlock.itemRegistration.get()) && !level.isClientSide()){
                DisxLogger.debug("Level is client side and player is holding lacquer block");
                if (player.isCrouching()){
                    DisxLogger.debug("Player is crouching; opening video id gui");
                    DisxServerPacketIndex.ServerPackets.openVideoIdScreen(player, blockHitResult.getBlockPos());
                    return InteractionResult.SUCCESS;
                } else if (blockEntity.getItem(0).isEmpty()){
                    DisxLogger.debug("Player is not crouching; putting in Lacquer Block");
                    player.getCooldowns().addCooldown(itemStack.getItem(), 9999999);
                    ItemStack newStack = itemStack.copyWithCount(1);
                    itemStack.shrink(1);
                    blockEntity.setItem(0, newStack, player);
                    level.playSound(null, blockPos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS);
                    blockEntity.setChanged();
                    player.getCooldowns().removeCooldown(newStack.getItem());
                    return InteractionResult.CONSUME;
                }
            } else if (!level.isClientSide()){
                DisxLogger.debug("Level is not client side");
                if (player.isCrouching()){
                    DisxLogger.debug("Player is crouching; opening video id gui");
                    DisxServerPacketIndex.ServerPackets.openVideoIdScreen(player, blockPos);
                    return InteractionResult.SUCCESS;
                } else if (itemStack.isEmpty()){
                    DisxLogger.debug("Main hand item is empty; checking if lacquer block is in already");
                    if (!blockEntity.getItem(0).isEmpty()){
                        DisxLogger.debug("Lacquer block was found; ejecting");
                        ItemStack returnToPlayer = blockEntity.removeItem(0, 1);
                        blockEntity.setItem(0, ItemStack.EMPTY);
                        ItemEntity itemEntity = new ItemEntity(level, ((double) blockPos.getX()) + 0.5, ((double) blockPos.getY()) + 0.2, ((double) blockPos.getZ()) + 0.5, returnToPlayer);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                        blockEntity.setChanged();
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        if (interactionHand.equals(InteractionHand.OFF_HAND) && !level.isClientSide()){
            DisxLogger.debug("Hand is off hand and level is not client side");
            if (player.isCrouching()){
                DisxLogger.debug("Player is crouching; opening video id gui");
                DisxServerPacketIndex.ServerPackets.openVideoIdScreen(player, blockPos);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.onBlockDestroy(level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    private void onBlockDestroy(Level level, BlockPos blockPos){
        DisxStampMakerEntity blockEntity = (DisxStampMakerEntity) level.getBlockEntity(blockPos);
        ItemStack item = blockEntity.getItem(0).copyWithCount(1);
        double x = blockPos.getX();
        double y = blockPos.getY();
        double z = blockPos.getZ();
        x += 0.5;
        y += 0.5;
        z += 0.5;
        ItemEntity itemEntity = new ItemEntity(level, x, y, z, item);
        level.addFreshEntity(itemEntity);
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        double x = blockPos.getX();
        double y = blockPos.getY();
        double z = blockPos.getZ();
        x += 0.5;
        y += 0.5;
        z += 0.5;
        ItemStack item2 = new ItemStack(itemRegistration.get());
        item2.setCount(1);
        ItemEntity itemEntity2 = new ItemEntity(serverLevel, x, y, z, item2);
        serverLevel.addFreshEntity(itemEntity2);
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return super.getTicker(level, blockState, blockEntityType);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T blockEntity) {
        return super.getListener(serverLevel, blockEntity);
    }

    @Override
    public Holder<Block> arch$holder() {
        return super.arch$holder();
    }

    @Override
    public @Nullable ResourceLocation arch$registryName() {
        return super.arch$registryName();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return super.isEnabled(featureFlagSet);
    }

    public static void registerBlock(Registrar<Block> blockRegistrar){
        blockRegistration = blockRegistrar.register(new ResourceLocation("disx", "stamp_maker"), () -> new DisxStampMaker(BlockBehaviour.Properties.copy(Blocks.PISTON)));
    }

    public static void registerBlockItem(Registrar<Item> itemRegistrar, RegistrySupplier<CreativeModeTab> tab){
        itemRegistration = itemRegistrar.register(
                (new ResourceLocation("disx", "stamp_maker")),
                () -> new BlockItem(blockRegistration.get(), new Item.Properties().arch$tab(tab)));
    }

    public static void registerBlockEntity(Registrar<BlockEntityType<?>> blockEntityRegistrar){
        blockEntityRegistration = blockEntityRegistrar.register(new ResourceLocation("disx","stamp_maker_entity"), () -> BlockEntityType.Builder.of(DisxStampMakerEntity::new, blockRegistration.get()).build(null));
    }

}
