package design.aeonic.multitool.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import design.aeonic.multitool.api.structure.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.awt.*;
import java.util.Objects;

public class Holograms {

    public static void drawStructureHologram(Minecraft minecraft, PoseStack stack, float partialTick, BlockPos pos, Vector3f renderPos, Direction direction, BuildableStructure structure, StructureInfo info, boolean drawBoundingBox) {
        Vector3f renderVec = renderPos == null ? new Vector3f(pos.getX(), pos.getY(), pos.getZ()) : renderPos.copy();
        BuildableStructure.PlacementState state = structure.checkPlacement(Objects.requireNonNull(minecraft.level), pos, info, direction);
        if (state == BuildableStructure.PlacementState.INVALID) return;
        drawStructureHologram(minecraft, stack, partialTick, renderVec, structure, info, state, direction, drawBoundingBox);
    }

    public static void drawStructureHologram(Minecraft minecraft, PoseStack stack, float partialTicks, Vector3f pos, BuildableStructure structure, StructureInfo info, BuildableStructure.PlacementState state, Direction direction, boolean drawBoundingBox) {
        // TODO: Optimize rendering - move rotations outside of rendering methods to first call
        if (state == BuildableStructure.PlacementState.INVALID) return;

        Color color = state == BuildableStructure.PlacementState.OK ? getValidColor() : getOccludedColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = getOpacity();

        RenderUtils.drawStructure(stack, minecraft.renderBuffers().bufferSource(), partialTicks, pos, structure, info, direction, r, g, b, a, drawBoundingBox);
    }

    // TODO: Server + client configs

    public static int getMaxPlacementDistance() {
        return 64;
    }

    public static Color getValidColor() {
        return Color.decode("#8899ff");
    }

    public static Color getOccludedColor() {
        return Color.decode("#ff6666");
    }

    public static float getOpacity() {
        return .6f;
    }
}
