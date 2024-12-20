# Lifecycle

## `setupPlayer(options: PlayerOptions)`

Initializes the player with the specified options. These options do not apply to all platforms, see chart below.

These options are different than the ones set using `updateOptions()`. Options other than those listed below will not be applied.

You should always call this function (even without any options set) before using the player to make sure everything is initialized. Do not call this more than once in the lifetime of your app.

Note that on Android this method must only be called while the app is in the foreground, otherwise it will throw an error with code `'android_cannot_setup_player_in_background'`. In this case you can wait for the app to be in the foreground and try again.

**Returns:** `Promise`

| Param                | Type     | Description   | Default   | Android | iOS | Web |
| -------------------- | -------- | ------------- | --------- | :-----: | :-: | :-----: |
| options              | `PlayerOptions` | The options   |
| options.minBuffer    | `number` | Minimum time in seconds that needs to be buffered | 15 (android), automatic (ios) | ✅ | ✅ | ❌ |
| options.maxBuffer    | `number` | Maximum time in seconds that needs to be buffered | 50 | ✅ | ❌ | ❌ |
| options.playBuffer   | `number` | Minimum time in seconds that needs to be buffered to start playing | 2.5 | ✅ | ❌ | ❌ |
| options.backBuffer   | `number` | Time in seconds that should be kept in the buffer behind the current playhead time. | 0 | ✅ | ❌ | ❌ |
| options.maxCacheSize | `number` | Maximum cache size in kilobytes | 0 | ✅ | ❌ | ❌ |
| options.androidAudioContentType  | `AndroidAudioContentType` | The audio content type indicates to the android system how you intend to use audio in your app. | `AndroidAudioContentType.Music` | ✅ | ❌ | ❌ |
| options.iosCategory  | `IOSCategory` | [AVAudioSession.Category](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616615-category) for iOS. Sets on `play()` | `IOSCategory.Playback` | ❌ | ✅ | ❌ |
| options.iosCategoryOptions | `IOSCategoryOptions[]` | [AVAudioSession.CategoryOptions](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616503-categoryoptions) for iOS. Sets on `play()` | `[]` | ❌ | ✅ | ❌ |
| options.iosCategoryMode  | `IOSCategoryMode` | [AVAudioSession.Mode](https://developer.apple.com/documentation/avfoundation/avaudiosession/1616508-mode) for iOS. Sets on `play()` | `default` | ❌ | ✅ | ❌ |
| options.autoHandleInterruptions   | `boolean` | Indicates whether the player should automatically handle audio interruptions. | false | ✅ | ✅  | ❌ |
| options.autoUpdateMetadata   | `boolean` | Indicates whether the player should automatically update now playing metadata data in control center / notification. | true | ✅ | ✅ | ❌ |

## `registerPlaybackService(serviceProvider)`

Register the playback service. The service will run as long as the player runs.

This function should only be called once, and should be registered right after registering your React application with `AppRegistry`.

You should use the playback service to register the event handlers that must be directly tied to the player, as the playback service might keep running when the app is in background.

| Param   | Type     | Description   |
| ------- | -------- | ------------- |
| serviceProvider | `function` | The function that must return an async service function. |

## `useTrackPlayerEvents(events: Event[], handler: Handler)`

Hook that fires on the specified events.

You can find a list of events in the [events section](../events.md#player).
