# Player

## `setupPlayer(options)`

Accepts a [`PlayerOptions`](../objects/player-options.md) object.

## `updateOptions(options)`

Accepts a [`UpdateOptions`](../objects/update-options.md) object. Updates
the configuration for the components.


⚠️ These parameters are different than the ones set using `setupPlayer()`.
Parameters other than those listed below will not be applied.

## `play()`

Plays or resumes the current track.

## `pause()`

Pauses the current track.

## `stop()`

Stops playback. Behavior is the same as `TrackPlayer.pause()` where
`playWhenReady` becomes `false`, but instead of just pausing playback, the item
is unloaded.

This function causes any further loading / buffering to stop.

## `retry()`

Retries the current track when it stopped playing due to a playback error.

## `seekBy(offset)`

Seeks by a relative time offset in the current track.

| Param   | Type     | Description             |
| ------- | -------- | ----------------------- |
| offset | `number` | The offset in seconds |

**Returns:** `Promise<void>`

## `seekTo(seconds)`

Seeks to a specified time position in the current track.

| Param   | Type     | Description             |
| ------- | -------- | ----------------------- |
| seconds | `number` | The position in seconds |

**Returns:** `Promise<void>`

## `setVolume(volume)`

Sets the volume of the player.

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| volume | `number` | The volume in a range from 0 to 1 |

**Returns:** `Promise<void>`

## `getVolume()`

Gets the volume of the player (a number between 0 and 1).

**Returns:** `Promise<number>`

## `setRate(rate)`
Sets the playback rate

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| rate   | `number` | The playback rate where 1 is the regular speed |

**Note:** If your rate is high, e.g. above 2, you may want to set the track's `pitchAlgorithm` to something like `PitchAlgorithm.Voice`, or else the default pitch algorithm (which in `SwiftAudioEx` drops down to `AVAudioTimePitchAlgorithm.lowQualityZeroLatency`) will likely
drop words in your audio.

## `getRate()`

Gets the playback rate, where 1 is the regular speed.

**Returns:** `Promise<number>`

## `getProgress()`

Gets the playback [`Progress`](../objects/progress.md) of the active track.

**Returns:** `Promise<`[Progress](../objects/progress.md)`>`

## `getPlaybackState()`

Gets the [`PlaybackState`](../objects/playback-state.md) of the player.

**Returns:** `Promise<`[PlaybackState](../objects/playback-state.md)`>`

## `getPlayWhenReady()`

Gets the current state of `playWhenReady`.

**Returns:** `Promise<boolean>`

## `setPlayWhenReady(playWhenReady)`

`TrackPlayer.setPlayWhenReady(false)` is the equivalent of `TrackPlayer.pause()`
and `TrackPlayer.setPlayWhenReady(true)` is the equivalent of
`TrackPlayer.play()`.

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| playWhenReady | `boolean` | A boolean representing if you want `playWhenReady` set or not. |
