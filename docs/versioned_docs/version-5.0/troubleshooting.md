---
sidebar_position: 10
---

# Troubleshooting

## iOS: (Enable Swift) `library not found for -lswiftCoreAudio for architecture x86_64`
Because the iOS module uses Swift, if the user is using a standard react-native application they'll need to add support for Swift in the project. This can easily be done by adding a swift file to the Xcode project -- could be called `dummy.swift` and saying yes when prompted if you'd like to generate a bridging header.

![Importing Swift](https://i.imgur.com/CBqBcWs.png)

## Android: `CIRCULAR REFERENCE:com.android.tools.r8.ApiLevelException: Default interface methods are only supported starting with Android N (--min-api 24)`
Since version 1.0.0, we began using a few Java 8 features in the project to reduce the code size.

To fix the issue, add the following options to your `android/app/build.gradle` file:
```diff
android {
    ...
+   compileOptions {
+       sourceCompatibility JavaVersion.VERSION_1_8
+       targetCompatibility JavaVersion.VERSION_1_8
+   }
    ...
}
```

## Android: `com.facebook.react.common.JavascriptException: No task registered for key TrackPlayer`
The playback service requires a headless task to be registered. You have to register it with `registerPlaybackService`.

## Android: `Error: Attribute XXX from [androidx.core:core:XXX] is also present at [com.android.support:support-compat:XXX]`
This error occurs when you're mixing both AndroidX and the Support Library in the same project.

You have to either upgrade everything to AndroidX or downgrade everything to the support library.


* For react-native-track-player, the last version to run the support library is **1.1.4** and the first version to run AndroidX is **1.2.0**.
* For react-native, the last version to run the support library is **0.59** and the first version to run AndroidX is **0.60**.

You can also use [jetifier](https://github.com/mikehardy/jetifier#usage-for-source-files) to convert all of the native code to use only one of them.

## Android: Cleartext HTTP traffic not permitted

Since API 28, Android disables traffic without TLS. To fix the issue you have to use `https` or [enable clear text traffic](https://stackoverflow.com/a/50834600).

## Web: Issues with HLS Streams

If your HLS content uses MPEG2-TS, you may need to enable transmuxing. The
only browsers capable of playing TS natively are Edge and Chromecast. You will
get a `CONTENT_UNSUPPORTED_BY_BROWSER` error on other browsers due to their lack
of TS support.

You can enable transmuxing by including [mux.js](https://www.npmjs.com/package/mux.js)
v5.6.3+ in your application. Once installed mux.js will be auto-detected and
will be used use to transmux TS content into MP4 on-the-fly, so that the content
can be played by the browser.

NOTE: there are some limitations to `mux.js` so not all possible content codec's
are supported.
