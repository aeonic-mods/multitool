package design.aeonic.multitool.api.ui.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import design.aeonic.multitool.api.client.RenderUtils;
import design.aeonic.multitool.api.ui.ISelectable;
import design.aeonic.multitool.registry.EMSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A sorta-abstract implementation of a radial selection screen.
 * This class can be constructed directly, but won't do anything with the new selection.<br /><br />
 * You probably want to override {@link #onClose()} in a child class and do something with {@link #selectedIndex}.
 * @param <T> a generic type of the class that implements this one, for passing custom data to {@link ISelectable}s.
 */
@SuppressWarnings("unchecked")
public class RadialSelectScreen<T extends RadialSelectScreen<T, S>, S extends ISelectable<T, S>> extends Screen {
    protected LocalPlayer player;
    protected double timeOpened = 0;
    protected double lastHoverSound = 0;

    protected final List<S> options;
    /**
     * The selected index.
     * Keep in mind that <b>this can be the index of an option that is not allowed for the current player.</b>
     */
    protected int selectedIndex = 0;

    public RadialSelectScreen(Component title, Collection<S> options) {
        super(title);

        // Parent screen hasn't been init yet, so we use getInstance here instead of the minecraft field
        this.player = Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance()).player);
        this.options = options.stream().filter(opt -> opt.showInSelectionScreen(player, (T) this)).toList();
    }

    @Override
    protected void init() {
        timeOpened = Blaze3D.getTime();
        playOpenSound();
    }

    /**
     * Be sure to call the super implementation here if you override!
     * Note again that the selectedIndex at this point has not been verified. DIY!
     */
    @Override
    public void onClose() {
        playCloseSound();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        // If the right mouse button was only clicked and not held, close the ui the second time it's clicked
        // The delay check is just to ensure the screen isn't closing instantly for any reason.
        if (pButton == 0 && Blaze3D.getTime() - timeOpened > .05f)
            onClose();
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        // If the right mouse button was held for a minimum amount of time, close the ui when it's released
        if (pButton == 1 && Blaze3D.getTime() - timeOpened > .2f)
            onClose();
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        double time = Blaze3D.getTime();
        int hovered = getHoveredOptionIndex(pMouseX, pMouseY);
        // If enough time has gone by since the last hover sound, play the hover sound
        if (hovered != this.selectedIndex && options.get(hovered).allowedForPlayer(player, (T) this)) {
            playSelectSound();
            lastHoverSound = time;
        }
        // Set the new selected index regardless of whether it's allowed for the player
        // You should be checking this in #onClose()
        this.selectedIndex = hovered;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        pPoseStack.pushPose();
        pPoseStack.translate(getCenterX(), getCenterY(), getZOffset());
        RenderSystem.defaultBlendFunc();

        // Draw the background arcs
        // We use two loops so section backgrounds won't overlap larger text/icons
        for (int i = 0; i < getNumSections(); i++) {
            Color color = getSectionBackgroundColor(i);

            float size = getSectionDegrees();
            float start = size * i;
            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;
            float a = getSectionOpacity(i);

            // We need to call enableBlend() each time we draw, or else it gets reset
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderUtils.drawArc(pPoseStack, getInnerRadius(i), getOuterRadius(i), start, size, r, g, b, a);
        }
        // Draw the icons + labels
        for (int i = 0; i < getNumSections(); i++) {
            S option = options.get(i);
            Component displayName = option.getDisplayName();

            double iconAngle = Math.toRadians((i + .5f) * getSectionDegrees());
            double iconRadius = (getOuterRadius(i) - getInnerRadius(i)) / 2d + getInnerRadius(i);
            int iconX = (int) (Math.cos(iconAngle) * iconRadius + getCenterX());
            int iconY = (int) (Math.sin(iconAngle) * iconRadius + getCenterY());
            int labelColor = (option.allowedForPlayer(player, (T) this) ? getLabelColor() : getDisabledLabelColor()).getRGB();

            if (displayName != TextComponent.EMPTY) {
                option.drawHudIcon(pPoseStack, iconX, iconY - getElementYOffset());
                drawCenteredString(pPoseStack, font, displayName, iconX - (int) getCenterX(), iconY - (int) getCenterY() + getElementYOffset(), labelColor);
            } else
                option.drawHudIcon(pPoseStack, iconX, iconY);
        }

        pPoseStack.popPose();
    }

    /**
     * Gets the Y offset between section labels and icons. Actual offset is this value * 2
     */
    protected int getElementYOffset() {
        return 5;
    }

    protected Color getSectionBackgroundColor(int sectionIndex) {
        return options.get(sectionIndex).allowedForPlayer(player, (T) this) ?
                (selectedIndex == sectionIndex ? getSelectedBackgroundColor() : getBackgroundColor()) :
                getDisabledBackgroundColor();
    }

    protected float getSectionOpacity(int sectionIndex) {
        return options.get(sectionIndex).allowedForPlayer(player, (T) this) ?
                (selectedIndex == sectionIndex ? getSelectedBackgroundOpacity() : getBackgroundOpacity()) :
                getDisabledBackgroundOpacity();
    }

    protected float getCenterX() {
        return width / 2f;
    }

    protected float getCenterY() {
        return height / 2f;
    }

    /**
     * Gets the z offset to render the screen at.
     * Delegated to a method in case we need to change it for some reason.
     */
    protected float getZOffset() {
        return 0;
    }

    protected int getHoveredOptionIndex(double mouseX, double mouseY) {
        return getHoveredOptionIndex(getMouseAngleDegrees(mouseX, mouseY));
    }

    protected int getHoveredOptionIndex(int mouseAngle) {
        return mouseAngle / getSectionDegrees();
    }

    protected int getMouseAngleDegrees(double mouseX, double mouseY) {
        return Math.floorMod((int) Math.toDegrees(Math.atan2(
                mouseY - height / 2f, mouseX - width / 2f)), 360);
    }

    protected int getSectionDegrees() {
        return 360 / getNumSections();
    }

    protected int getNumSections() {
        return options.size();
    }

    protected void playOpenSound() {
        player.playSound(EMSounds.MULTITOOL_OPEN.get(), getSoundVolume(), 1f);
    }

    protected void playSelectSound() {
        player.playSound(EMSounds.MULTITOOL_SELECT.get(), getSoundVolume(), 1f);
    }

    protected void playCloseSound() {
        player.playSound(EMSounds.MULTITOOL_CLOSE.get(), getSoundVolume(), 1f);
    }

    // TODO: Client config options

    protected int getInnerRadius(int section) {
        return 40 + (section == selectedIndex ? selectedArcOffset() : 0);
    }

    protected int getOuterRadius(int section) {
        return 80 + (section == selectedIndex ? selectedArcOffset() : 0);
    }

    protected int selectedArcOffset() {
        return 2;
    }

    protected Color getLabelColor() {
        return Color.decode("#ffffff");
    }

    protected Color getDisabledLabelColor() {
        return Color.decode("#a9a9a9");
    }

    protected Color getBackgroundColor() {
        return Color.decode("#13171e");
    }

    protected Color getSelectedBackgroundColor() {
        return Color.decode("#004cf0");
    }

    protected Color getDisabledBackgroundColor() {
        return Color.decode("#2b2c2d");
    }

    protected float getBackgroundOpacity() {
        return .75f;
    }

    protected float getSelectedBackgroundOpacity() {
        return .75f;
    }

    protected float getDisabledBackgroundOpacity() {
        return .6f;
    }

    protected float getSoundVolume() {
        return 1f;
    }
}
