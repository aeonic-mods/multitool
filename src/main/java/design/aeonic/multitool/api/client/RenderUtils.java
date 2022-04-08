package design.aeonic.multitool.api.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import design.aeonic.multitool.api.ui.RadialSelectScreen;
import net.minecraft.client.renderer.GameRenderer;

public class RenderUtils {

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
