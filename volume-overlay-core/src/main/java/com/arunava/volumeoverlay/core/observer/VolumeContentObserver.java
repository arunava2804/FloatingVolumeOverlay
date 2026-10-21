package com.arunava.volumeoverlay.core.observer;

import android.database.ContentObserver;
import android.os.Handler;
import com.arunava.volumeoverlay.core.controller.VolumeController;

/**
 * ContentObserver to detect when the device volume is altered by external apps,
 * Bluetooth accessories, or phone system settings.
 */
public class VolumeContentObserver extends ContentObserver {

    public interface OnVolumeChangeListener {
        void onVolumeChanged(int newPercentage, boolean isMuted);
    }

    private final VolumeController volumeController;
    private final OnVolumeChangeListener listener;

    public VolumeContentObserver(Handler handler, VolumeController controller, OnVolumeChangeListener listener) {
        super(handler);
        this.volumeController = controller;
        this.listener = listener;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (listener != null && volumeController != null) {
            int percentage = volumeController.getMediaVolumePercentage();
            boolean isMuted = volumeController.isMuted();
            listener.onVolumeChanged(percentage, isMuted);
        }
    }
}
