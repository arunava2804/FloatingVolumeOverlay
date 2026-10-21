package com.arunava.volumeoverlay.demo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arunava.volumeoverlay.core.VolumeOverlayManager;

/**
 * Modern MVVM ViewModel managing state for overlay activity and permission status.
 */
public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isOverlayRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasOverlayPermission = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application) {
        super(application);
        refreshStatus();
    }

    public LiveData<Boolean> getIsOverlayRunning() {
        return isOverlayRunning;
    }

    public LiveData<Boolean> getHasOverlayPermission() {
        return hasOverlayPermission;
    }

    public void refreshStatus() {
        boolean permissionGranted = VolumeOverlayManager.getInstance().hasOverlayPermission(getApplication());
        hasOverlayPermission.setValue(permissionGranted);

        boolean running = VolumeOverlayManager.getInstance().isRunning();
        isOverlayRunning.setValue(running);
    }

    public void setOverlayRunning(boolean running) {
        isOverlayRunning.setValue(running);
    }
}
