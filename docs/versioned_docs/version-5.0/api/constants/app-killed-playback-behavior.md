---
sidebar_position: 6
---

# App Killed Playback Behavior (android-only)

```ts
import { AppKilledPlaybackBehavior } from 'react-native-track-player';
```

## `ContinuePlayback` (default)

This option will continue playing audio in the background when the app is
removed from recents. The notification remains. This is the default.

## `PausePlayback`

This option will pause playing audio in the background when the app is removed
from recents. The notification remains and can be used to resume playback.

## `StopPlaybackAndRemoveNotification`

This option will stop playing audio in the background when the app is removed
from recents. The notification is removed and can't be used to resume playback.
Users would need to open the app again to start playing audio.
