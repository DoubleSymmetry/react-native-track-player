# Queue

## `add(tracks, insertBeforeIndex)`
Adds one or more tracks to the queue.

**Returns:** `Promise<number | void>` - The promise resolves with the first
added track index. If no tracks were added it returns `void`.

| Param          | Type     | Description   |
| -------------- | -------- | ------------- |
| tracks         | `Track \| Track[]` | The [Track](../objects/track.md) objects that will be added |
| insertBeforeIndex | `number` | The index of the track that will be located immediately after the inserted tracks. Set it to `null` to add it at the end of the queue |

## `remove(tracks)`
Removes one or more tracks from the queue. If the current track is removed, the next track will activated. If the current track was the last track in the queue, the first track will be activated.

**Returns:** `Promise<void>`

| Param  | Type              | Description |
|--------|-------------------|-------------|
| tracks | `Track \| Track[]` | The [Track](../objects/track.md) objects that will be removed |

## `setQueue(tracks)`

Clears the current queue and adds the supplied tracks to the now empty queue.

**Returns:** `Promise<void>`

| Param  | Type              | Description |
|--------|-------------------|-------------|
| tracks | `Track[]` | An array of [Track](../objects/track.md) to replace the current queue with. |

## `load(track)`

Replaces the current track with the supplied track or creates a track when the queue is empty.

| Param  | Type              | Description |
|--------|-------------------|-------------|
| track | `Track`            | The [Track](../objects/track.md) object that will be loaded |

## `skip(index, initialPosition)`
Skips to a track in the queue.

**Returns:** `Promise<void>`

| Param  | Type     | Description     |
| ------ | -------- | --------------- |
| index  | `number` | The track index |
| initialPosition | `number` | **Optional.** Sets the initial playback for the track you're skipping to. |

## `skipToNext(initialPosition)`
Skips to the next track in the queue.

**Returns:** `Promise<void>`

| Param  | Type     | Description     |
| ------ | -------- | --------------- |
| initialPosition | `number` | **Optional.** Sets the initial playback for the track you're skipping to. |

## `skipToPrevious(initialPosition)`
Skips to the previous track in the queue.

**Returns:** `Promise<void>`

| Param  | Type     | Description     |
| ------ | -------- | --------------- |
| initialPosition | `number` | **Optional.** Sets the initial playback for the track you're skipping to. |

## `move(fromIndex, toIndex)`

Moves a track from the specified index to another.

| Param  | Type     | Description     |
| ------ | -------- | --------------- |
| fromIndex | `number` | The index of the track you'd like to move. |
| toIndex   | `number` | The position you'd like to move the track to. |


## `reset()`
Resets the player stopping the current track and clearing the queue.

## `getTrack(index)`
Gets a track object from the queue.

**Returns:** `Promise<`[Track](../objects/track.md)`>`

| Param    | Type       | Description     |
| -------- | ---------- | --------------- |
| index    | `number`   | The track index |

## `getActiveTrack()`

Gets the active track object.

**Returns:** `Promise<`[Track](../objects/track.md)` | undefined>`

## `getActiveTrackIndex()`

Gets the index of the current track, or `undefined` if no track loaded

**Returns:** `Promise<number | undefined>`

## `getQueue()`
Gets the whole queue

**Returns:** `Promise<`[Track[]](../objects/track.md)`>`

## `removeUpcomingTracks()`
Clears any upcoming tracks from the queue.

## `updateMetadataForTrack(index, metadata)`
Updates the metadata of a track in the queue.
If the current track is updated, the notification and the Now Playing Center will be updated accordingly.

**Returns:** `Promise<void>`

| Param    | Type       | Description   |
| -------- | ---------- | ------------- |
| index    | `number`   | The track index  |
| metadata | `object`   | A subset of the [Track Object](../objects/track.md) with only the `artwork`, `title`, `artist`, `album`, `description`, `genre`, `date`, `rating` and `duration` properties. |

## `setRepeatMode(mode)`
Sets the repeat mode.

| Param    | Type       | Description     |
| -------- | ---------- | --------------- |
| mode     | [Repeat Mode](../constants/repeat-mode.md) | The repeat mode |

## `getRepeatMode()`
Gets the repeat mode.

**Returns:** [Repeat Mode](../constants/repeat-mode.md)

## ⚠️ `getCurrentTrack()`

**⚠️ Deprecated:** To get the active track index use
[`getActiveTrackIndex()`](#getactivetrackindex) instead or use
[`getActiveTrack()`](#getactivetrack) to get the active track object.

Gets the index of the current track, or null if no track loaded

**Returns:** `Promise<number | null>`

