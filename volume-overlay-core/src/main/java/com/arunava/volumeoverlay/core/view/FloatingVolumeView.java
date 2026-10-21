package com.arunava.volumeoverlay.core.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.SeekBar;
import androidx.core.content.ContextCompat;

import com.arunava.volumeoverlay.core.R;
import com.arunava.volumeoverlay.core.controller.VolumeController;
import com.arunava.volumeoverlay.core.databinding.ViewExpandedVolumePanelBinding;
import com.arunava.volumeoverlay.core.databinding.ViewFloatingVolumeBinding;
import com.arunava.volumeoverlay.core.model.OverlayConfig;

/**
 * Floating on-screen view manager handling gesture dragging, edge docking,
 * volume button controls (+/-), and the multi-stream expanded popup.
 */
public class FloatingVolumeView {

    private final Context context;
    private final WindowManager windowManager;
    private final VolumeController volumeController;
    private final OverlayConfig config;

    private ViewFloatingVolumeBinding binding;
    private WindowManager.LayoutParams windowParams;

    // Expanded panel popup
    private ViewExpandedVolumePanelBinding panelBinding;
    private WindowManager.LayoutParams panelParams;
    private boolean isPanelOpen = false;

    // Gesture tracking variables
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private final int touchSlop;

    public FloatingVolumeView(Context context, WindowManager windowManager, VolumeController controller, OverlayConfig config) {
        this.context = context;
        this.windowManager = windowManager;
        this.volumeController = controller;
        this.config = config != null ? config : OverlayConfig.createDefault();
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewFloatingVolumeBinding.inflate(inflater, null, false);

        // Apply transparency
        binding.layoutOverlayContainer.setAlpha(config.getAlpha());

        // Determine Window Overlay Type based on Android version
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            @SuppressWarnings("deprecation")
            int deprecatedType = WindowManager.LayoutParams.TYPE_PHONE;
            layoutType = deprecatedType;
        }

        windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        windowParams.gravity = Gravity.TOP | Gravity.START;
        windowParams.x = config.getInitialX();
        windowParams.y = config.getInitialY();

        setupDragListener();
        setupButtonListeners();
        updateVolumeDisplay(volumeController.getMediaVolumePercentage(), volumeController.isMuted());

        windowManager.addView(binding.getRoot(), windowParams);
    }

    private void setupDragListener() {
        binding.ivDragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x;
                        initialY = windowParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        windowParams.x = initialX + deltaX;
                        windowParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(binding.getRoot(), windowParams);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (config.isAutoEdgeDocking()) {
                            snapToNearestEdge();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void snapToNearestEdge() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int viewWidth = binding.getRoot().getWidth();

        int centerX = windowParams.x + (viewWidth / 2);
        if (centerX < screenWidth / 2) {
            windowParams.x = 0; // Snap to left edge
        } else {
            windowParams.x = screenWidth - viewWidth; // Snap to right edge
        }
        windowManager.updateViewLayout(binding.getRoot(), windowParams);
    }

    private void setupButtonListeners() {
        // Volume Up (+)
        binding.btnVolumeUp.setOnClickListener(v -> {
            int percentage = volumeController.volumeUp(config.isHapticFeedbackEnabled());
            updateVolumeDisplay(percentage, volumeController.isMuted());
        });

        // Volume Down (-)
        binding.btnVolumeDown.setOnClickListener(v -> {
            int percentage = volumeController.volumeDown(config.isHapticFeedbackEnabled());
            updateVolumeDisplay(percentage, volumeController.isMuted());
        });

        // Mute / Unmute
        binding.btnVolumeMute.setOnClickListener(v -> {
            boolean isMuted = volumeController.toggleMute(config.isHapticFeedbackEnabled());
            updateVolumeDisplay(volumeController.getMediaVolumePercentage(), isMuted);
        });

        // Expand Multi-Stream Panel
        binding.btnExpandPanel.setOnClickListener(v -> toggleExpandedPanel());
    }

    public void updateVolumeDisplay(int percentage, boolean isMuted) {
        if (binding == null) return;

        if (config.isShowPercentage()) {
            binding.tvVolumePercentage.setVisibility(View.VISIBLE);
            binding.tvVolumePercentage.setText(percentage + "%");
        } else {
            binding.tvVolumePercentage.setVisibility(View.GONE);
        }

        binding.pbVolumeLevel.setProgress(percentage);

        if (isMuted) {
            binding.btnVolumeMute.setColorFilter(ContextCompat.getColor(context, R.color.overlay_mute_red));
        } else {
            binding.btnVolumeMute.setColorFilter(ContextCompat.getColor(context, R.color.overlay_icon_tint));
        }
    }

    private void toggleExpandedPanel() {
        if (isPanelOpen) {
            closeExpandedPanel();
        } else {
            openExpandedPanel();
        }
    }

    private void openExpandedPanel() {
        if (isPanelOpen) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        panelBinding = ViewExpandedVolumePanelBinding.inflate(inflater, null, false);

        int layoutType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        panelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        panelParams.gravity = Gravity.CENTER;

        // Configure Media SeekBar
        initSeekBar(panelBinding.sbMediaVolume, AudioManager.STREAM_MUSIC);
        // Configure Ringtone SeekBar
        initSeekBar(panelBinding.sbRingVolume, AudioManager.STREAM_RING);
        // Configure Call SeekBar
        initSeekBar(panelBinding.sbCallVolume, AudioManager.STREAM_VOICE_CALL);
        // Configure Alarm SeekBar
        initSeekBar(panelBinding.sbAlarmVolume, AudioManager.STREAM_ALARM);

        panelBinding.btnClosePanel.setOnClickListener(v -> closeExpandedPanel());

        windowManager.addView(panelBinding.getRoot(), panelParams);
        isPanelOpen = true;
    }

    private void initSeekBar(SeekBar seekBar, int streamType) {
        int max = volumeController.getStreamMaxVolume(streamType);
        int current = volumeController.getStreamVolume(streamType);
        seekBar.setMax(max);
        seekBar.setProgress(current);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) {
                    volumeController.setStreamVolume(streamType, progress);
                    if (streamType == AudioManager.STREAM_MUSIC) {
                        updateVolumeDisplay(volumeController.getMediaVolumePercentage(), volumeController.isMuted());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {}

            @Override
            public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    public void closeExpandedPanel() {
        if (isPanelOpen && panelBinding != null) {
            try {
                windowManager.removeView(panelBinding.getRoot());
            } catch (Exception ignored) {}
            panelBinding = null;
            isPanelOpen = false;
        }
    }

    public void remove() {
        closeExpandedPanel();
        if (binding != null && binding.getRoot().isAttachedToWindow()) {
            try {
                windowManager.removeView(binding.getRoot());
            } catch (Exception ignored) {}
            binding = null;
        }
    }
}
