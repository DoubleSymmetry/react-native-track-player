---
sidebar_position: 8
---

# Migrating from v3.2 to v4

### General Additions

1. **New Function:** [`getActiveTrackIndex()`](./api/functions/queue.md#getactivetrackindex)
  - Description: Gets the index of the current track, or `undefined` if no track loaded.
2. **New Function:** [`getProgress()`](./api/functions/player.md#getprogress)
  - Description: Returns progress, buffer and duration information.
3. **New Function:**  [`getPlaybackState`](./api/functions/player.md#getplaybackstate)
  - Description: Returns the current playback state.
4. New Events: [`Event.AudioChapterMetadataReceived`, `Event.AudioTimedMetadataReceived`, `Event.AudioCommonMetadataReceived`](./api/events.md#metadata)
  - Description: More detailed metadata events that are emitted when metadata is received from the native player.

### General Changes

- The configuration option `alwaysPauseOnInterruption` has been moved to the `android` section of options.

```diff
await TrackPlayer.updateOptions({
+      android: {
+        alwaysPauseOnInterruption: true,
+      },
-      alwaysPauseOnInterruption: true,
}
```

- On iOS, the pitch algorithm now defaults to `timeDomain` instead of `lowQualityZeroLatency`. The latter has been deprecated by Apple and has known issues on iOS 17.

### Swift Compatibility

In order to support iOS 12 (12.4 still officially supported by react-native), make SwiftUI optional.

In XCode add "-weak_framework" and "SwiftUI" to the "Other Linker Flags" build settings.

### Hook Behavior Updates

The [`usePlaybackState()`](./api/hooks.md##useplaybackstate) hook now initially returns `{ state: undefined }` before it has finished retrieving the current state. It previously returned [`State.None`](./api/constants/state.md), indicating no track loaded.

### Player Method Updates

- The [`remove()`](./api/functions/queue.md#removeracks) function now supports removing the current track. If the current track is removed, the next track in the queue will be activated. If the current track was the last track in the queue, the first track will be activated.

The [`getTrack()`](./api/functions/queue.md#gettrack) function now returns `undefined` instead of `null`.

### Player State Updates
- New player states have been introduced and some updated
1. [`State.Error`](./api/constants/state.md)
  - **New.** Emitted when an error state is encountered.
2. [`State.Ended`](./api/constants/state.md)
  - **New.** State indicates playback stopped due to the end of the queue being reached.
3. [`State.Loading`](./api/constants/state.md)
  - **New.** State indicating the initial loading phase of a track.
4. [`State.Buffering`](./api/constants/state.md)
  - **Updated.** Now emitted no matter whether playback is paused or not.
5. [`State.Connecting`](./api/constants/state.md)
  - **Deprecated.** Please use `State.Loading` instead.

### General Deprecations
- The following functions and events have been deprecated:

1. `getState()` - Please use the `state` property returned by [`getPlaybackState()`](./api/functions/player.md#getplaybackstate).
2. `getDuration()` -  Please use the `duration` property returned by [`getProgress()`](./api/functions/player.md#getprogress).
3. `getPosition()` -  Please use the `position` property returned by [`getProgress()`](./api/functions/player.md#getprogress).
4. `getBufferedPosition()` -  Please use the `buffered` property returned by [`getProgress()`](./api/functions/player.md#getprogress).
5. `getCurrentTrack()` - Please use [`getActiveTrackIndex()`](./api/functions/queue.md#getactivetrackindex).
6. `Event.PlaybackTrackChanged` - Please use [`Event.PlaybackActiveTrackChanged`](./api/events.md#playbackactivetrackchanged). Also note that in 4.0 `Event.PlaybackTrackChanged` is no longer emitted when a track repeats.
7. `Event.PlaybackMetadataReceived` - Please use [`Event.AudioChapterMetadataReceived`, `Event.AudioTimedMetadataReceived`, `Event.AudioCommonMetadataReceived`](./api/events.md#metadata).

### Removals

- The clearMetadata() function has been removed. Instead, use [`reset()`](./api/functions/player.md#reset), which stops playback, clears the queue, and clears the notification.

### Typescript Imports

1. If you were using deep imports from RNTP, the `src` has been completely
reorganized, and so you may need to adjust your imports accordingly. If you've
been importing everything directly (ex. `import ... from 'react-native-track-player';`)
then you don't need to do anything.
1. The `PlaybackStateEvent` interface has been renamed to `PlaybackState`
