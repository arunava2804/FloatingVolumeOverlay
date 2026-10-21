package com.arunava.volumeoverlay.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

import com.arunava.volumeoverlay.core.model.OverlayConfig;
import com.arunava.volumeoverlay.core.service.VolumeOverlayService;

/**
 * Public SDK Entry Point for the Floating Volume Overlay Library.
 * Provides straightforward 1-line methods to check permissions, start, and stop the overlay.
 *
 * <pre>
 * // Usage Example:
 * if (VolumeOverlayManager.getInstance().hasOverlayPermission(context)) {
 *     VolumeOverlayManager.getInstance().startOverlay(context, OverlayConfig.createDefault());
 * } else {
 *     VolumeOverlayManager.getInstance().requestOverlayPermission(activity);
 * }
 * </pre>
 */
public class VolumeOverlayManager {

    private static volatile VolumeOverlayManager instance;

    private VolumeOverlayManager() {}

    public static VolumeOverlayManager getInstance() {
        if (instance == null) {
            synchronized (VolumeOverlayManager.class) {
                if (instance == null) {
                    instance = new VolumeOverlayManager();
                }
            }
        }
        return instance;
    }

    /**
     * Check if the device has granted the SYSTEM_ALERT_WINDOW (Display over other apps) permission.
     */
    public boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * Launch the system settings screen for the user to grant the overlay permission.
     */
    public void requestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName())
            );
            activity.startActivity(intent);
        }
    }

    /**
     * Start the floating volume overlay with custom configuration.
     */
    public void startOverlay(Context context, OverlayConfig config) {
        if (!hasOverlayPermission(context)) {
            return;
        }

        Intent intent = new Intent(context, VolumeOverlayService.class);
        intent.setAction(VolumeOverlayService.ACTION_START);
        if (config != null) {
            intent.putExtra(VolumeOverlayService.EXTRA_CONFIG, config);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * Start the floating volume overlay with default settings.
     */
    public void startOverlay(Context context) {
        startOverlay(context, OverlayConfig.createDefault());
    }

    /**
     * Stop the floating volume overlay and terminate the service.
     */
    public void stopOverlay(Context context) {
        Intent intent = new Intent(context, VolumeOverlayService.class);
        intent.setAction(VolumeOverlayService.ACTION_STOP);
        context.stopService(intent);
    }

    /**
     * Returns true if the floating overlay service is currently active.
     */
    public boolean isRunning() {
        return VolumeOverlayService.isRunning();
    }
}
