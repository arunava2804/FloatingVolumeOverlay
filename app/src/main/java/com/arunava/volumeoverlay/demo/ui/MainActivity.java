package com.arunava.volumeoverlay.demo.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.arunava.volumeoverlay.core.VolumeOverlayManager;
import com.arunava.volumeoverlay.core.controller.VolumeController;
import com.arunava.volumeoverlay.core.model.OverlayConfig;
import com.arunava.volumeoverlay.demo.R;
import com.arunava.volumeoverlay.demo.databinding.ActivityMainBinding;
import com.arunava.volumeoverlay.demo.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private VolumeController volumeController;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission is recommended to keep service alive", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        volumeController = new VolumeController(this);

        setupObservers();
        setupListeners();
        checkNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshStatus();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupObservers() {
        // Observe permission status
        viewModel.getHasOverlayPermission().observe(this, hasPermission -> {
            if (hasPermission) {
                binding.cardPermission.setVisibility(View.GONE);
                binding.switchOverlayToggle.setEnabled(true);
            } else {
                binding.cardPermission.setVisibility(View.VISIBLE);
                binding.switchOverlayToggle.setEnabled(false);
                binding.switchOverlayToggle.setChecked(false);
            }
        });

        // Observe running state
        viewModel.getIsOverlayRunning().observe(this, isRunning -> {
            binding.switchOverlayToggle.setChecked(isRunning);
            if (isRunning) {
                binding.tvStatusBadge.setText(R.string.status_running);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_active));
            } else {
                binding.tvStatusBadge.setText(R.string.status_stopped);
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_inactive));
            }
        });
    }

    private void setupListeners() {
        // Overlay permission button
        binding.btnGrantPermission.setOnClickListener(v -> {
            VolumeOverlayManager.getInstance().requestOverlayPermission(this);
        });

        // Switch toggle
        binding.switchOverlayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // Ignore programmatic updates

            if (isChecked) {
                if (VolumeOverlayManager.getInstance().hasOverlayPermission(this)) {
                    startConfiguredOverlay();
                    viewModel.setOverlayRunning(true);
                } else {
                    buttonView.setChecked(false);
                    VolumeOverlayManager.getInstance().requestOverlayPermission(this);
                }
            } else {
                VolumeOverlayManager.getInstance().stopOverlay(this);
                viewModel.setOverlayRunning(false);
            }
        });

        // Quick hardware volume test buttons
        binding.btnQuickVolUp.setOnClickListener(v -> {
            int pct = volumeController.volumeUp(true);
            Toast.makeText(this, "Media Volume: " + pct + "%", Toast.LENGTH_SHORT).show();
        });

        binding.btnQuickVolDown.setOnClickListener(v -> {
            int pct = volumeController.volumeDown(true);
            Toast.makeText(this, "Media Volume: " + pct + "%", Toast.LENGTH_SHORT).show();
        });

        binding.btnQuickMute.setOnClickListener(v -> {
            boolean isMuted = volumeController.toggleMute(true);
            Toast.makeText(this, isMuted ? "Volume Muted" : "Volume Unmuted", Toast.LENGTH_SHORT).show();
        });

        // Open settings
        binding.btnOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void startConfiguredOverlay() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
        int alphaProgress = prefs.getInt(SettingsActivity.KEY_ALPHA, 95);
        boolean edgeDock = prefs.getBoolean(SettingsActivity.KEY_EDGE_DOCK, true);
        boolean haptic = prefs.getBoolean(SettingsActivity.KEY_HAPTIC, true);
        boolean showPercentage = prefs.getBoolean(SettingsActivity.KEY_SHOW_PERCENTAGE, true);

        OverlayConfig config = new OverlayConfig.Builder()
                .setAlpha(alphaProgress / 100.0f)
                .setAutoEdgeDocking(edgeDock)
                .setHapticFeedbackEnabled(haptic)
                .setShowPercentage(showPercentage)
                .build();

        VolumeOverlayManager.getInstance().startOverlay(this, config);
    }
}
