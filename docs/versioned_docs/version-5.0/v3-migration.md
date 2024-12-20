---
sidebar_position: 6
---

# Migrating from v2 to v3

Due to how Android handles foreground services, it's not possible for us to stop the process manually, as it's waiting for the foreground service to come back. With v3 we are introducing the following changes related to this:

- On Android, the audio service can't be manually stopped by the app anymore.
    The OS itself decides when to stop it.
- An audio control notification will *always* be present (depending on phone
    vendor, this would look and behave differently), which allows users to
    quickly go back to the app by tapping on it.

The full changelog of added features and bug fixes [can be found here](https://github.com/doublesymmetry/react-native-track-player/releases/tag/v3.0).

When migrating from v2 to v3, the following has changed:


## API Changes

### `stopWithApp` is now `android.appKilledPlaybackBehavior` 

```diff
// Methods
await TrackPlayer.updateOptions({
-      stopWithApp: true,
+      android: {
+        appKilledPlaybackBehavior: AppKilledPlaybackBehavior.ContinuePlayback
+      }
});
```

### `destroy` and `stop` have been removed

```diff
// remove all usages of `.destroy()` and `.stop()`
-  TrackPlayer.destroy();
-  TrackPlayer.stop();
```

## Configuration Changes

### `track-player.json` / Build Preferences no longer needed

HLS, Dash, & Smoothstreaming are now supported on Android out of the box. You
can remove your `track-player.json` file if you have one. You still need to
ensure that [the correct `type` is specified on your `Track`
object](./api/objects/track.md).

```diff
- track-player.json
```

### Minimum Compile/Target SDK

You also need to have a minimum compile & target SDK of 31 (Android 12)

```groovy
// android/build.gradle
...
    compileSdkVersion = 31
    targetSdkVersion = 31
...
```
