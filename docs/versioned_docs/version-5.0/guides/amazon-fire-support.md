---
sidebar_position: 99
---

# Amazon FireOS Support

Support for Android in `react-native-track-player` is built on top of the [ExoPlayer](https://github.com/google/ExoPlayer) media player library provided by Google. ExoPlayer does not officially support Amazon's FireOS fork of Android, because it does not pass [Android CTS](https://source.android.com/compatibility/cts). ExoPlayer seems to work decently on FireOS 5, but it hardly works at all on FireOS 4.

Thankfully, [Amazon maintains](https://developer.amazon.com/docs/fire-tv/media-players.html#exoplayer) a [ported version of ExoPlayer](https://github.com/amzn/exoplayer-amazon-port) that can be used as a direct replacement as long as matching versions are used.

## Setup

In order to fully support FireOS, you will need to build separate APKs for Google and Amazon. This can be accomplised using gradle flavors.

You will need to choose a ExoPlayer version that has been ported by Amazon, and that is close enough to the version that `react-native-track-player` currently uses, in order to compile. In this example we have chosen to use `2.9.0`.

### Edit `app/build.gradle`

Add `productFlavors` to your build file:

```
android {
  flavorDimensions "store"
  productFlavors {
    google {
      dimension "store"
    }
    amazon {
      dimension "store"
    }
  }
  ...
}
```

Override the exoplayer library, and version, by modifying the dependencies:

```
dependencies {
  compile (project(':react-native-track-player')) {
    exclude group: 'com.google.android.exoplayer'
  }
  googleImplementation 'com.google.android.exoplayer:exoplayer-core:2.10.1'
  amazonImplementation 'com.amazon.android:exoplayer-core:2.10.1'
  ...
}
```

### Build Using Variants

To make builds using either Google or Amazon libraries, you will need to specify a build variant when you build.

Here are some examples of `react-native` commands using the `--variant` parameter that can be added as scripts in `package.json`:

```
"scripts": {
  "android-google": "react-native run-android --variant=googleDebug",
  "android-amazon": "react-native run-android --variant=amazonDebug",
  "android-release-google": "react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle && react-native run-android --variant=googleRelease",
  "android-release-amazon": "react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle && react-native run-android --variant=amazonRelease",
  ...
}
```
