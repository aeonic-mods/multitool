package design.aeonic.multitool.api;

import com.mojang.math.Vector3f;
import design.aeonic.multitool.EngineersMultitool;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Some vector utility methods, mostly for structure building.
 */
public class Vectors {
    public static Direction rotateFaceFromNorth(Direction face, Direction direction) {
        return switch (direction) {
            case EAST -> face.getClockWise();
            case SOUTH -> face.getOpposite();
            case WEST -> face.getCounterClockWise();
            default -> face;
        };
    }

    public static Vec3i rotateFromNorth(Vec3i toRotate, Vec3i rotateAround, Direction direction) {
        Vector3f rot = rotateFromNorth(new Vector3f(toRotate.getX(), toRotate.getY(), toRotate.getZ()), new Vector3f(rotateAround.getX(), rotateAround.getY(), rotateAround.getZ()), direction);
        return new Vec3i(Math.round(rot.x()), Math.round(rot.y()), Math.round(rot.z()));
    }

    public static Vec3i rotateFromNorth(Vec3i toRotate, Direction direction) {
        Vector3f rot = rotateFromNorth(new Vector3f(toRotate.getX(), toRotate.getY(), toRotate.getZ()), direction);
        return new Vec3i(Math.round(rot.x()), Math.round(rot.y()), Math.round(rot.z()));
    }

    public static Vector3f rotateFromNorth(Vector3f toRotate, Vector3f rotateAround, Direction direction) {
        var adj = toRotate.copy();
        adj.sub(rotateAround);
        var ret = rotateFromNorth(adj, direction);
        ret.add(rotateAround);
        return ret;
    }

    public static Vector3f rotateFromNorth(Vector3f toRotate, Direction direction) {
        return rotate(toRotate, direction.toYRot() - 180f);
    }

    public static Vector3f rotate(Vector3f toRotate, float degrees) {
        Vector3f ret = toRotate.copy();
        ret.transform(Vector3f.YP.rotationDegrees(- degrees));
        return ret;
    }
}
