package com.arunava.volumeoverlay.core.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.arunava.volumeoverlay.core.R;
import com.arunava.volumeoverlay.core.controller.VolumeController;
import com.arunava.volumeoverlay.core.model.OverlayConfig;
import com.arunava.volumeoverlay.core.observer.VolumeContentObserver;
import com.arunava.volumeoverlay.core.view.FloatingVolumeView;

/**
 * Foreground Service that manages the lifecycle of the Floating Volume Overlay
 * and keeps it active even when the host application is in the background.
 */
public class VolumeOverlayService extends Service {

    public static final String ACTION_START = "com.arunava.volumeoverlay.ACTION_START";
    public static final String ACTION_STOP = "com.arunava.volumeoverlay.ACTION_STOP";
    public static final String EXTRA_CONFIG = "extra_overlay_config";

    private static final String CHANNEL_ID = "channel_floating_volume_service";
    private static final int NOTIFICATION_ID = 2804;

    private static boolean isServiceRunning = false;

    private FloatingVolumeView floatingView;
    private VolumeController volumeController;
    private VolumeContentObserver volumeObserver;

    public static boolean isRunning() {
        return isServiceRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        volumeController = new VolumeController(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        OverlayConfig config = null;
        if (intent != null && intent.hasExtra(EXTRA_CONFIG)) {
            config = (OverlayConfig) intent.getSerializableExtra(EXTRA_CONFIG);
        }
        if (config == null) {
            config = OverlayConfig.createDefault();
        }

        startInForeground();
        initializeOverlay(config);
        registerVolumeObserver();

        isServiceRunning = true;
        return START_STICKY;
    }

    private void startInForeground() {
        createNotificationChannel();

        Intent stopIntent = new Intent(this, VolumeOverlayService.class);
        stopIntent.setAction(ACTION_STOP);
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, pendingFlags);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.volume_overlay_notification_title))
                .setContentText(getString(R.string.volume_overlay_notification_text))
                .setSmallIcon(R.drawable.ic_volume_speaker)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.volume_overlay_action_stop), stopPendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int serviceType = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
            }
            startForeground(NOTIFICATION_ID, notification, serviceType);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.volume_overlay_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.volume_overlay_channel_desc));
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void initializeOverlay(OverlayConfig config) {
        if (floatingView != null) {
            floatingView.remove();
        }
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = new FloatingVolumeView(this, windowManager, volumeController, config);
        floatingView.show();
    }

    private void registerVolumeObserver() {
        if (volumeObserver == null) {
            Handler handler = new Handler(Looper.getMainLooper());
            volumeObserver = new VolumeContentObserver(handler, volumeController, (newPercentage, isMuted) -> {
                if (floatingView != null) {
                    floatingView.updateVolumeDisplay(newPercentage, isMuted);
                }
            });
            getContentResolver().registerContentObserver(
                    Settings.System.CONTENT_URI,
                    true,
                    volumeObserver
            );
        }
    }

    private void unregisterVolumeObserver() {
        if (volumeObserver != null) {
            try {
                getContentResolver().unregisterContentObserver(volumeObserver);
            } catch (Exception ignored) {}
            volumeObserver = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        unregisterVolumeObserver();
        if (floatingView != null) {
            floatingView.remove();
            floatingView = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
