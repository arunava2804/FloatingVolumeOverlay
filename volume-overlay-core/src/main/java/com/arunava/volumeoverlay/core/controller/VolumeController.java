package com.arunava.volumeoverlay.core.controller;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/**
 * Controller class managing Android AudioManager and hardware volume adjustments.
 */
public class VolumeController {

    private final AudioManager audioManager;
    private final Context context;
    private int preMuteVolume = -1;

    public VolumeController(Context context) {
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Increase the primary media volume by 1 step.
     */
    public int volumeUp(boolean hapticFeedback) {
        if (audioManager == null) return 0;
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_PLAY_SOUND
        );
        triggerHaptic(hapticFeedback);
        return getMediaVolumePercentage();
    }

    /**
     * Decrease the primary media volume by 1 step.
     */
    public int volumeDown(boolean hapticFeedback) {
        if (audioManager == null) return 0;
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_PLAY_SOUND
        );
        triggerHaptic(hapticFeedback);
        return getMediaVolumePercentage();
    }

    /**
     * Toggle mute state for Media stream.
     * @return true if now muted, false if unmuted.
     */
    public boolean toggleMute(boolean hapticFeedback) {
        if (audioManager == null) return false;
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (current > 0) {
            preMuteVolume = current;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
            triggerHaptic(hapticFeedback);
            return true;
        } else {
            int restore = (preMuteVolume > 0) ? preMuteVolume : (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restore, AudioManager.FLAG_PLAY_SOUND);
            triggerHaptic(hapticFeedback);
            return false;
        }
    }

    public boolean isMuted() {
        if (audioManager == null) return false;
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
    }

    public int getMediaVolumePercentage() {
        if (audioManager == null) return 0;
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (max == 0) return 0;
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return (int) (((float) current / max) * 100);
    }

    public int getStreamVolume(int streamType) {
        if (audioManager == null) return 0;
        return audioManager.getStreamVolume(streamType);
    }

    public int getStreamMaxVolume(int streamType) {
        if (audioManager == null) return 1;
        return audioManager.getStreamMaxVolume(streamType);
    }

    public void setStreamVolume(int streamType, int volume) {
        if (audioManager == null) return;
        audioManager.setStreamVolume(streamType, volume, 0);
    }

    private void triggerHaptic(boolean enabled) {
        if (!enabled) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    Vibrator vibrator = vibratorManager.getDefaultVibrator();
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
                }
            } else {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(20);
                    }
                }
            }
        } catch (Exception ignored) {
            // Haptic is non-critical
        }
    }
}
