package xyz.ar06.disx.blocks;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DisxStampMakerShapes {
    public static VoxelShape makeShape0(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0.625, 0.125, 0.875, 0.6875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.9375, 0.875, 0.6875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0, 0.875, 0.0625, 0.9375), BooleanOp.OR);

        return shape;
    }

    public static VoxelShape makeShape90(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.625, 0.125, 0.875, 0.6875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 0.0625, 0.6875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.125, 1, 0.0625, 0.875), BooleanOp.OR);

        return shape;
    }

    public static VoxelShape makeShape180(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0.625, 0.0625, 0.875, 0.6875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0, 0.875, 0.6875, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.0625, 0.875, 0.0625, 1), BooleanOp.OR);

        return shape;
    }

    public static VoxelShape makeShape270(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0.625, 0.125, 0.9375, 0.6875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0, 0.125, 1, 0.6875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 0.9375, 0.0625, 0.875), BooleanOp.OR);

        return shape;
    }
}
