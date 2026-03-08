package xyz.ar06.disx.blocks;

import dev.architectury.registry.fuel.FuelRegistry;
import xyz.ar06.disx.entities.DisxRecordPressEntity;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.items.DisxLacquerDrop;

public class DisxLacquerBlock extends HalfTransparentBlock{

    public static RegistrySupplier<Block> blockRegistration;
    public static RegistrySupplier<Item> itemRegistration;

    public DisxLacquerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        entity.makeStuckInBlock(blockState, new Vec3(0.5, 0.10, 0.5));
        super.entityInside(blockState, level, blockPos, entity);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.box(0,0,0,1,1,1);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.onBlockDestroy(level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    private void onBlockDestroy(Level level, BlockPos blockPos){
        if (!level.isClientSide()) {

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
        ItemStack item2 = new ItemStack(itemRegistration.get());
        item2.setCount(1);
        ItemEntity itemEntity2 = new ItemEntity(serverLevel, x, y, z, item2);
        serverLevel.addFreshEntity(itemEntity2);
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
    }

    public static void registerBlock(Registrar<Block> registrar){
        blockRegistration = registrar.register(
                new ResourceLocation("disx","lacquer_block"),
                () -> new DisxLacquerBlock(
                        BlockBehaviour.Properties.of()
                                .destroyTime(4f)
                                .friction(2f)
                                .noCollission()
                                .jumpFactor(0.1f)
                                .speedFactor(0.1f)
                                .sound(SoundType.HONEY_BLOCK)
                                .noOcclusion()
                )
        );
    }

    public static void registerBlockItem(Registrar<Item> registrar, RegistrySupplier<CreativeModeTab> tab){
        itemRegistration = registrar.register(
                new ResourceLocation("disx","lacquer_block"),
                () -> new BlockItem(blockRegistration.get(), new Item.Properties()
                        .arch$tab(tab)
                )
        );
        //FuelRegistry.register(900, itemRegistration.get());
    }
}
