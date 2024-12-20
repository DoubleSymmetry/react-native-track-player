# PlaybackErrorEvent

An object denoting a playback error encountered during loading or playback of a
track.

| Property | Type     | Description |
|----------|----------|-------------|
| code     | `string` | The code values are strings prefixed with `android_` on Android and `ios_` on iOS. |
| message  | `string` | The error message emitted by the native player. |
