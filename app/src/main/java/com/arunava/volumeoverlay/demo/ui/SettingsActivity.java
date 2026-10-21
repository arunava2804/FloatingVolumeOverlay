package com.arunava.volumeoverlay.demo.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.arunava.volumeoverlay.core.VolumeOverlayManager;
import com.arunava.volumeoverlay.core.model.OverlayConfig;
import com.arunava.volumeoverlay.demo.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_NAME = "prefs_volume_overlay";
    public static final String KEY_ALPHA = "pref_alpha";
    public static final String KEY_EDGE_DOCK = "pref_edge_dock";
    public static final String KEY_HAPTIC = "pref_haptic";
    public static final String KEY_SHOW_PERCENTAGE = "pref_show_percentage";

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        loadSavedSettings();
        setupListeners();
    }

    private void loadSavedSettings() {
        int alphaProgress = prefs.getInt(KEY_ALPHA, 95);
        boolean edgeDock = prefs.getBoolean(KEY_EDGE_DOCK, true);
        boolean haptic = prefs.getBoolean(KEY_HAPTIC, true);
        boolean showPercentage = prefs.getBoolean(KEY_SHOW_PERCENTAGE, true);

        binding.sbTransparency.setProgress(alphaProgress);
        binding.tvAlphaValue.setText(alphaProgress + "%");
        binding.switchEdgeDock.setChecked(edgeDock);
        binding.switchHaptic.setChecked(haptic);
        binding.switchPercentage.setChecked(showPercentage);
    }

    private void setupListeners() {
        binding.sbTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int safeProgress = Math.max(25, progress);
                binding.tvAlphaValue.setText(safeProgress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.btnApplySettings.setOnClickListener(v -> saveAndApplySettings());
    }

    private void saveAndApplySettings() {
        int alphaProgress = Math.max(25, binding.sbTransparency.getProgress());
        boolean edgeDock = binding.switchEdgeDock.isChecked();
        boolean haptic = binding.switchHaptic.isChecked();
        boolean showPercentage = binding.switchPercentage.isChecked();

        prefs.edit()
                .putInt(KEY_ALPHA, alphaProgress)
                .putBoolean(KEY_EDGE_DOCK, edgeDock)
                .putBoolean(KEY_HAPTIC, haptic)
                .putBoolean(KEY_SHOW_PERCENTAGE, showPercentage)
                .apply();

        OverlayConfig config = new OverlayConfig.Builder()
                .setAlpha(alphaProgress / 100.0f)
                .setAutoEdgeDocking(edgeDock)
                .setHapticFeedbackEnabled(haptic)
                .setShowPercentage(showPercentage)
                .build();

        if (VolumeOverlayManager.getInstance().isRunning()) {
            VolumeOverlayManager.getInstance().stopOverlay(this);
            VolumeOverlayManager.getInstance().startOverlay(this, config);
        }

        Toast.makeText(this, "Settings Applied Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
