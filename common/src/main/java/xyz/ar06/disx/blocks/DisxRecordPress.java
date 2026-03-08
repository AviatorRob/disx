package xyz.ar06.disx.blocks;

import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.DisxMain;
import xyz.ar06.disx.entities.DisxRecordPressEntity;
import xyz.ar06.disx.items.DisxBlankDisc;
import xyz.ar06.disx.items.DisxCustomDisc;
import xyz.ar06.disx.items.DisxRecordStamp;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DisxRecordPress extends BaseEntityBlock {

    static DirectionProperty facing = DirectionProperty.create("facing");
    VoxelShape blockShape = Shapes.box(0.03125, 0, 0.0625, 0.96875, 0.875, 0.9375);

    static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> blockItemRegistration;

    protected DisxRecordPress(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facing);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockShape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return blockShape;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getHorizontalDirection();
        return this.defaultBlockState().setValue(facing, direction);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DisxRecordPressEntity(blockPos, blockState);
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
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
        int powerLevel = level.getBestNeighborSignal(blockPos);
        DisxRecordPressEntity blockEntity = (DisxRecordPressEntity) level.getBlockEntity(blockPos);
        blockEntity.setPowerInput(powerLevel);
        blockEntity.setChanged();
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (interactionHand == InteractionHand.MAIN_HAND && !level.isClientSide()){
            ItemStack inHandStack = player.getMainHandItem();
            DisxRecordPressEntity blockEntity = (DisxRecordPressEntity) level.getBlockEntity(blockPos);
            Item inHandItem = inHandStack.getItem();
            Registrar<Item> itemRegistrar = DisxMain.REGISTRAR_MANAGER.get().get(Registries.ITEM);
            if (inHandItem.equals(itemRegistrar.get(new ResourceLocation("disx", "blank_disc")))){
                DisxLogger.debug("item in hand is blank disc");
                if (blockEntity.getItem(0).isEmpty()){
                    ItemStack inHandStackCopy = inHandStack.copyWithCount(1);
                    inHandStack.shrink(1);
                    level.playSound(null, blockPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    blockEntity.setItem(0, inHandStackCopy);
                }
            } else if (inHandItem.equals(itemRegistrar.get(new ResourceLocation("disx", "record_stamp")))){
                DisxLogger.debug("item in hand is record stamp");
                if (blockEntity.getItem(1).isEmpty()){
                    ItemStack inHandStackCopy = inHandStack.copyWithCount(1);
                    inHandStack.shrink(1);
                    level.playSound(null, blockPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS);
                    blockEntity.setItem(1, inHandStackCopy);
                }
            } else if(inHandStack.isEmpty()){
                DisxLogger.debug("item in hand is empty");
                for (int i = 0; i <= 2; i++){
                    ItemStack item = blockEntity.getItem(i);
                    if (!item.isEmpty()){
                        ItemStack newStack = item.copyWithCount(1);
                        blockEntity.removeItem(i, 1);
                        ItemEntity itemEntity = new ItemEntity(level, ((double) blockPos.getX()) + 0.5, ((double) blockPos.getY()) + 0.2, ((double) blockPos.getZ()) + 0.5, newStack);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                        level.playSound(null, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS);
                    }
                }
            } else {
                for (Item compare : DisxCustomDisc.validDiscFactors){
                    if (inHandItem.equals(compare)){
                        DisxLogger.debug("item in hand is disc factor");
                        DisxLogger.debug(blockEntity.getItem(2));
                        if (blockEntity.getItem(2).isEmpty()){
                            ItemStack inHandStackCopy = inHandStack.copyWithCount(1);
                            inHandStack.shrink(1);
                            level.playSound(null, blockPos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS);
                            blockEntity.setItem(2, inHandStackCopy);
                        }
                    }
                }
            }
            DisxLogger.debug("item in hand is none of the above");
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.onBlockDestroy(level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    private void onBlockDestroy(Level level, BlockPos blockPos){
        if (!level.isClientSide()) {
            DisxRecordPressEntity blockEntity = (DisxRecordPressEntity) level.getBlockEntity(blockPos);
            ItemStack item0 = blockEntity.getItem(0).copyWithCount(1);
            ItemStack item1 = blockEntity.getItem(1).copyWithCount(1);
            ItemStack item2 = blockEntity.getItem(2).copyWithCount(1);
            double x = blockPos.getX();
            double y = blockPos.getY();
            double z = blockPos.getZ();
            x += 0.5;
            y += 0.5;
            z += 0.5;
            ItemEntity itemEntity0 = new ItemEntity(level, x, y, z, item0);
            ItemEntity itemEntity1 = new ItemEntity(level, x, y, z, item1);
            ItemEntity itemEntity2 = new ItemEntity(level, x, y, z, item2);
            level.addFreshEntity(itemEntity0);
            level.addFreshEntity(itemEntity1);
            level.addFreshEntity(itemEntity2);
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

    public static void registerBlock(Registrar<Block> registrar){
        blockRegistration = registrar.register(
                new ResourceLocation("disx", "record_press"),
                () -> new DisxRecordPress(BlockBehaviour.Properties.copy(Blocks.PISTON))
        );
    }

    public static void registerBlockItem(Registrar<Item> registrar, RegistrySupplier<CreativeModeTab> tab){
        blockItemRegistration = registrar.register(
                new ResourceLocation("disx", "record_press"),
                () -> new BlockItem(blockRegistration.get(), new Item.Properties().arch$tab(tab))
        );
    }

    public static void registerBlockEntity(Registrar<BlockEntityType<?>> registrar){
        registrar.register(
                new ResourceLocation("disx","record_press_entity"),
                () -> BlockEntityType.Builder.of(
                        DisxRecordPressEntity::new,
                        blockRegistration.get())
                        .build(null)
        );
    }

}
