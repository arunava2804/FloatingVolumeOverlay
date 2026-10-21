package com.arunava.volumeoverlay.core.model;

import java.io.Serializable;

/**
 * Configuration options for the Floating Volume Overlay.
 * Allows customization of transparency, position, edge snapping, and haptic feedback.
 */
public class OverlayConfig implements Serializable {

    private final float alpha;
    private final boolean autoEdgeDocking;
    private final boolean hapticFeedbackEnabled;
    private final int initialX;
    private final int initialY;
    private final boolean showPercentage;

    private OverlayConfig(Builder builder) {
        this.alpha = builder.alpha;
        this.autoEdgeDocking = builder.autoEdgeDocking;
        this.hapticFeedbackEnabled = builder.hapticFeedbackEnabled;
        this.initialX = builder.initialX;
        this.initialY = builder.initialY;
        this.showPercentage = builder.showPercentage;
    }

    public static OverlayConfig createDefault() {
        return new Builder().build();
    }

    public float getAlpha() {
        return alpha;
    }

    public boolean isAutoEdgeDocking() {
        return autoEdgeDocking;
    }

    public boolean isHapticFeedbackEnabled() {
        return hapticFeedbackEnabled;
    }

    public int getInitialX() {
        return initialX;
    }

    public int getInitialY() {
        return initialY;
    }

    public boolean isShowPercentage() {
        return showPercentage;
    }

    public static class Builder {
        private float alpha = 0.95f;
        private boolean autoEdgeDocking = true;
        private boolean hapticFeedbackEnabled = true;
        private int initialX = 0;
        private int initialY = 300;
        private boolean showPercentage = true;

        public Builder setAlpha(float alpha) {
            this.alpha = Math.max(0.2f, Math.min(1.0f, alpha));
            return this;
        }

        public Builder setAutoEdgeDocking(boolean autoEdgeDocking) {
            this.autoEdgeDocking = autoEdgeDocking;
            return this;
        }

        public Builder setHapticFeedbackEnabled(boolean enabled) {
            this.hapticFeedbackEnabled = enabled;
            return this;
        }

        public Builder setInitialPosition(int x, int y) {
            this.initialX = x;
            this.initialY = y;
            return this;
        }

        public Builder setShowPercentage(boolean show) {
            this.showPercentage = show;
            return this;
        }

        public OverlayConfig build() {
            return new OverlayConfig(this);
        }
    }
}
