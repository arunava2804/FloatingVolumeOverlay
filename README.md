# AruAndroid: Assistive Floating Volume Key Overlay 🔊

[![Android API](https://img.shields.io/badge/API-24%2B%20(Android%207.0%2B)-brightgreen.svg)](https://android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-100%25%20Java-orange.svg)](https://www.oracle.com/java/)
[![UI](https://img.shields.io/badge/UI-Modern%20XML%20%2B%20ViewBinding-blueviolet.svg)](https://developer.android.com/topic/libraries/view-binding)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVVM-success.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An enterprise-grade, modular, accessibility-focused Android system overlay application and reusable SDK designed for smartphones and tablets where physical **Volume Up (`+`)** and **Volume Down (`-`)** buttons are broken, unresponsive, or difficult to reach.

---

## 🌟 Why This Project? (Real-World Impact)

Physical hardware buttons often degrade, get jammed, or break entirely from daily wear and tear. When this happens, users are forced to repeatedly dig through device settings just to adjust video, call, or game volume.

**AruAndroid** solves this problem by rendering an ergonomic, draggable on-screen floating capsule that sits safely over any app, game, or browser, granting instant one-tap volume control with zero performance or battery penalty.

---

## 🏗️ Architecture & Modular Design

The repository is built strictly with **100% Java**, **XML Layouts**, and **Clean MVVM Architecture**, structured as a decoupled multi-module SDK:

```
AruAndroid/
├── volume-overlay-core/           # 📦 Reusable Library / SDK Module
│   ├── VolumeOverlayManager.java  # Singleton Public SDK Entry Point
│   ├── service/                   # Foreground Service with Android 14+ FGS Type
│   ├── view/                      # Floating Window with touch drag & edge docking physics
│   ├── controller/                # Android AudioManager wrapper (+, -, mute, haptics)
│   ├── observer/                  # ContentObserver for system-wide volume sync
│   └── model/                     # OverlayConfig model with Builder pattern
│
└── app/                           # 📱 Showcase Demo & Settings Application
    ├── ui/                        # MainActivity & SettingsActivity (ViewBinding)
    └── viewmodel/                 # MainViewModel (LiveData & State Management)
```

---

## 🚀 How to Use the SDK in Any Android Project

Adding the floating volume controller to any other application takes only **2 lines of Java code**:

### 1. Check & Request Overlay Permission
```java
if (!VolumeOverlayManager.getInstance().hasOverlayPermission(context)) {
    VolumeOverlayManager.getInstance().requestOverlayPermission(activity);
}
```

### 2. Start the Floating Overlay
```java
// Option A: Start with default settings
VolumeOverlayManager.getInstance().startOverlay(context);

// Option B: Start with custom configuration
OverlayConfig config = new OverlayConfig.Builder()
        .setAlpha(0.90f)               // 90% opacity
        .setAutoEdgeDocking(true)       // Snaps to screen edge on release
        .setHapticFeedbackEnabled(true) // Vibration click feel
        .setShowPercentage(true)        // Display live percentage (e.g. "70%")
        .build();

VolumeOverlayManager.getInstance().startOverlay(context, config);
```

### 3. Stop the Overlay
```java
VolumeOverlayManager.getInstance().stopOverlay(context);
```

---

## ⚡ Technical Highlights (Interview Talking Points)

- **Universal Android Compatibility:** Works seamlessly across Android 7.0 (API 24) through Android 14 & 15 (API 34/35).
- **Modern WindowManager Overlay:** Uses `TYPE_APPLICATION_OVERLAY` with `FLAG_NOT_FOCUSABLE` and `FLAG_LAYOUT_NO_LIMITS` for lag-free touch forwarding.
- **Android 14+ Foreground Service Compliance:** Uses `foregroundServiceType="specialUse"` with appropriate service subtype declaration and `POST_NOTIFICATIONS` permission handling.
- **Bi-directional Volume Sync:** Employs an Android `ContentObserver` on `Settings.System.CONTENT_URI` to guarantee the on-screen volume gauge updates instantly when volume is adjusted via Bluetooth headsets or in-app sliders.
- **Battery & Memory Optimization:** Efficient View recycling with zero background polling or battery drain during Doze mode.

---

## 🛠️ Build and Setup

1. Open **Android Studio** (Hedgehog, Iguana, Koala, Ladybug or newer).
2. Select **Open Project** and navigate to:
   ```
   C:\Users\HP ALL IN ONE\Android Studio Projects\AruAndroid
   ```
3. Let Gradle sync automatically with **JDK 17**.
4. Connect an Android device or emulator and click **Run 'app'**.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - see the file for details.

Developed with ❤️ by [Arunava (arunava2804)](https://github.com/arunava2804)
