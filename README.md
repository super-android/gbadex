# GBADex 🎮

A GBA emulator for Android, built DeX-first.

## Features (Roadmap)
- [x] ROM library with grid/list view
- [x] Material You theming (matches your wallpaper)
- [x] Samsung DeX resizable window support
- [ ] mGBA emulation core (Phase 3)
- [ ] Box art search & picker (Phase 5)
- [ ] Google Drive cloud saves (Phase 6)

## Development Setup (GitHub Codespaces)

### 1. Open in Codespaces
Click the green **Code** button on GitHub → **Codespaces** → **Create codespace on main**

### 2. Install Android SDK in the Codespace terminal
```bash
# Install Java 17
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Download Android command-line tools
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

# Set Android SDK env vars
echo 'export ANDROID_HOME=~/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
source ~/.bashrc

# Accept licenses and install SDK
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 3. Build the APK
```bash
cd /workspaces/gbadex
chmod +x gradlew
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Install on your Samsung
Download the APK from the Codespaces file browser, copy to your phone, then:
- Enable "Install unknown apps" for your browser in Settings
- Tap the APK to install

Or via ADB over Wi-Fi:
```bash
adb connect YOUR_PHONE_IP:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure
```
app/src/main/
├── java/com/superandroid/gbadex/
│   ├── MainActivity.kt          # Entry point, navigation
│   ├── data/
│   │   ├── model/Game.kt        # Game data model
│   │   └── repository/
│   │       └── RomRepository.kt # Scans folders for ROMs
│   ├── viewmodel/
│   │   └── LibraryViewModel.kt  # State management
│   └── ui/
│       ├── theme/Theme.kt       # Material You theme
│       └── screens/
│           └── LibraryScreen.kt # Main game library UI
└── AndroidManifest.xml
```
