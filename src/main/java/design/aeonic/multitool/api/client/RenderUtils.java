package design.aeonic.multitool.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import design.aeonic.multitool.api.Vectors;
import design.aeonic.multitool.api.structure.BuildableStructure;
import design.aeonic.multitool.api.structure.StructureInfo;
import design.aeonic.multitool.api.ui.RadialSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Objects;
import java.util.Random;

public class RenderUtils {

    /**
     * Renders a structure.
     */
    public static void drawStructure(PoseStack stack, MultiBufferSource.BufferSource source, float partialTicks, Vector3f position, BuildableStructure structure, StructureInfo info, Direction direction, float r, float g, float b, float a, boolean drawBoundingBox) {
        Minecraft minecraft = Objects.requireNonNull(Minecraft.getInstance());
        Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3i origin = structure.origin(direction);
        position.sub(new Vector3f(origin.getX(), origin.getY(), origin.getZ()));
        Vec3i size = Vectors.rotateFromNorth(info.size(), direction);
        // Don't ask me why this works. All we need to know is that it works. I swear to god it works.
        Vec3i offset = switch (direction) {
            case EAST   -> new Vec3i(1, 0, 0);
            case SOUTH  -> new Vec3i(1, 0, 1);
            case WEST   -> new Vec3i(0, 0, 1);
            default     -> new Vec3i(0, 0, 0);
        };

        stack.pushPose();
        stack.translate(- cameraPosition.x, - cameraPosition.y, - cameraPosition.z);
        stack.translate(position.x(), position.y(), position.z());

        drawStructure(stack, source, minecraft.getBlockRenderer(), structure, info.withDirection(direction), r, g, b, a);
        if (drawBoundingBox) {
            LevelRenderer.renderLineBox(stack, source.getBuffer(RenderType.lines()), offset.getX(), 0, offset.getZ(), size.getX() + offset.getX(), size.getY(), size.getZ() + offset.getZ(), r, g, b, Math.min(1, a * 1.5f));
            source.endBatch();
        }

        stack.popPose();
    }

    public static void drawStructure(PoseStack stack, MultiBufferSource.BufferSource source, BlockRenderDispatcher renderer, BuildableStructure structure, StructureInfo blocks, float r, float g, float b, float a) {
        VertexConsumer cons = source.getBuffer(RenderType.translucentMovingBlock());
        Random random = new Random();
        blocks.blockInfo().forEach(i -> drawRelativeBlock(stack, cons, renderer, random, i, r, g, b, a));
        source.endBatch();
    }

    public static void drawRelativeBlock(PoseStack stack, VertexConsumer cons, BlockRenderDispatcher renderer, Random random, StructureInfo.PosAndState block, float r, float g, float b, float a) {
        stack.pushPose();
        stack.translate(block.pos().getX() + .002f, block.pos().getY() + .002f, block.pos().getZ() + .002f);
        stack.scale(.996f, .996f, .996f);

        drawRelativeBlock(block.state(), stack, cons, renderer, random, r, g, b, a);

        stack.popPose();
    }

    public static void drawRelativeBlock(BlockState state, PoseStack stack, VertexConsumer cons, BlockRenderDispatcher renderer, Random random, float r, float g, float b, float a) {
        BakedModel model = renderer.getBlockModel(state);
        for (Direction side: Direction.values()) {
            random.setSeed(42L);
            model.getQuads(state, side, random, EmptyModelData.INSTANCE).forEach(quad -> cons.putBulkData(
                    stack.last(), quad, r, g, b, a, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
            ));
        }
        random.setSeed(42L);
        model.getQuads(state, null, random, EmptyModelData.INSTANCE).forEach(quad -> cons.putBulkData(
                stack.last(), quad, r, g, b, a, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        ));
    }

    /**
     * Draws an arc to the screen, used for {@link RadialSelectScreen} and related UIs.
     * @param stack the posestack (matrix stack)
     * @param innerRadius the radius to start at
     * @param outerRadius the radius to end at - not the width
     * @param start the start angle in degrees, where 0 is to the right
     * @param size the size of the arc, in degrees
     * @param r red
     * @param g green
     * @param b blue
     * @param a opacity
     */
    public static void drawArc(PoseStack stack, float innerRadius, float outerRadius, float start, float size, float r, float g, float b, float a) {
        // Adapted from Chisels and Bits radial selection
        int resolution = 720;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder vertexBuffer = Tesselator.getInstance().getBuilder();
        Matrix4f pose = stack.last().pose();
        vertexBuffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float num = resolution * (size / 360f);
        for (int i = 0; i <= num; i++)
        {
            float angle = (float) Math.toRadians(start + (i / (float) resolution) * 360f);
            vertexBuffer.vertex(pose, (float) (outerRadius * Math.cos(angle)), (float) (outerRadius * Math.sin(angle)), 0).color(r, g, b, a).endVertex();
            vertexBuffer.vertex(pose, (float) (innerRadius * Math.cos(angle)), (float) (innerRadius * Math.sin(angle)), 0).color(r, g, b, a).endVertex();
        }
        vertexBuffer.end();
        BufferUploader.end(vertexBuffer);
    }
}
