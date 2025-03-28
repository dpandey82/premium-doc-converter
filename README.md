# Premium Document Converter

A professional-grade document conversion Android application with 100% format preservation capabilities.

## Building the APK

### Option 1: Using GitHub Actions (Easiest)

1. This repository is configured with GitHub Actions to automatically build the APK.
2. After pushing code to GitHub, go to the "Actions" tab in your repository.
3. Wait for the workflow to complete (usually takes 3-5 minutes).
4. Click on the completed workflow.
5. Scroll down to the "Artifacts" section.
6. Download the "app-debug" artifact, which contains your APK file.

### Option 2: Building locally with Gradle

1. Make sure you have JDK 17 installed.
2. Open Terminal on your Mac.
3. Navigate to the project directory:
   ```
   cd path/to/premium-doc-converter
   ```
4. Make the Gradle wrapper executable:
   ```
   chmod +x gradlew
   ```
5. Build the APK:
   ```
   ./gradlew assembleDebug
   ```
6. Find the APK at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Installing on Samsung Galaxy S23 Ultra

1. Copy the APK file to your phone (using a USB cable, email, cloud storage, etc.)
2. On your phone, open the file manager and locate the APK
3. Tap the APK file. If prompted about security settings, go to Settings and enable "Install from unknown sources" for your file manager app.
4. Tap "Install" and wait for the installation to complete.
5. Once installed, you can open the app from your app drawer.

## Features

- Format preservation of all document types
- Verification system to ensure quality
- Support for numerous file formats
- Professional UI design
