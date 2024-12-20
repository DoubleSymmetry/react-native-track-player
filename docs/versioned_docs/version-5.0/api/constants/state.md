---
sidebar_position: 1
---

# State

All State types are made available through the named export `State`:

```ts
import { State } from 'react-native-track-player';
```

| Name | Description |
|------|-------------|
| `None`        | State indicating that no media is currently loaded |
| `Ready`       | State indicates that the player is paused, but ready to start playing |
| `Playing`     | State indicating that the player is currently playing |
| `Paused`      | State indicating that the player is currently paused |
| `Stopped`     | State indicating that the player is currently stopped |
| `Ended`       | State indicates playback stopped due to the end of the queue being reached |
| `Buffering`   | State indicating that the player is currently buffering (no matter whether playback is paused or not) |
| `Loading`     | State indicating the initial loading phase of a track |
| `Error`       | State indicating that the player experienced a playback error causing the audio to stop playing (or not start playing). When in `State.Error`, calling `play()` reloads the current track and seeks to its last known time. |
