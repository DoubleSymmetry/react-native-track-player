# PlayerOptions

All parameters are optional. You also only need to specify the ones you want to update.

| Param | Type | Description | Android | iOS |
|-------|------|-------------|---------|-----|
| `minBuffer` | `number` | Minimum duration of media that the player will attempt to buffer in seconds. | ✅ | ✅ |
| `maxBuffer` | `number` | Maximum duration of media that the player will attempt to buffer in seconds. | ✅ | ❌ |
| `backBuffer` | `number` | Duration in seconds that should be kept in the buffer behind the current playhead time. | ✅ | ❌ |
| `playBuffer` | `number` | Duration of media in seconds that must be buffered for playback to start or resume following a user action such as a seek. | ✅ | ❌ |
| `maxCacheSize` | `number` | Maximum cache size in kilobytes. | ✅ | ❌ |
| `iosCategory` | [`IOSCategory`](../constants/ios-category.md) | An [`IOSCategory`](../constants/ios-category.md). Sets on `play()`. | ❌ | ✅  |
| `iosCategoryMode` | [`IOSCategoryMode`](../constants/ios-category-mode.md) | The audio session mode, together with the audio session category, indicates to the system how you intend to use audio in your app. You can use a mode to configure the audio system for specific use cases such as video recording, voice or video chat, or audio analysis. Sets on `play()`. | ❌ | ✅  |
| `iosCategoryOptions` | [`IOSCategoryOptions[]`](../constants/ios-category-options.md) | An array of [`IOSCategoryOptions`](../constants/ios-category-options.md). Sets on `play()`. | ❌ | ✅  |
| `autoUpdateMetadata` | `boolean` | Indicates whether the player should automatically update now playing metadata data in control center / notification. Defaults to `true`. | ✅ | ✅ |
| `autoHandleInterruptions` | `boolean` | Indicates whether the player should automatically handle audio interruptions. Defaults to `false`. | ✅ | ✅ |
| `androidAudioContentType` | `boolean` | The audio content type indicates to the android system how you intend to use audio in your app. With `autoHandleInterruptions: true` and `androidAudioContentType: AndroidAudioContentType.Speech`, the audio will be paused during short interruptions, such as when a message arrives. Otherwise the playback volume is reduced while the notification is playing. Defaults to `AndroidAudioContentType.Music` | ✅ | ❌ |
