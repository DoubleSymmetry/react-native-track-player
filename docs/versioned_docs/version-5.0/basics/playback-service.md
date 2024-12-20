---
sidebar_position: 3
---

# Playback Service

The playback service keeps running even when the app is in the background. It will start when the player is set up and will only stop when the player is destroyed. It is a good idea to put any code in there that needs to be directly tied to the player state. For example, if you want to be able to track what is being played for analytics purposes, the playback service would be the place to do so.

## Remote Events

[Remote events](../api/events.md#media-controls) are sent from places outside of our user interface that we can react to. For example if the user presses the pause media control in the IOS lockscreen / Android notification or from their Bluetooth headset, we want to have TrackPlayer pause the audio.

If you create a listener to a remote event like `Event.RemotePause` in the context of a React component, there is a chance the UI will be unmounted automatically when the app is in the background, causing it to be missed. For this reason it is best to place remote listeners in the playback service, since it will keep running even when the app is in the background.

## Example
```js
import { PlaybackService } from './src/services';

// This needs to go right after you register the main component of your app
// AppRegistry.registerComponent(...)
TrackPlayer.registerPlaybackService(() => PlaybackService);
```

```ts
// src/services/PlaybackService.ts
import { Event } from 'react-native-track-player';

export const PlaybackService = async function() {

    TrackPlayer.addEventListener(Event.RemotePlay, () => TrackPlayer.play());

    TrackPlayer.addEventListener(Event.RemotePause, () => TrackPlayer.pause());

    // ...

};
```

## Custom Media Controls Notification id & name

react-native-track-player is using media controls.
As a result, it creates a notification channel.
 - More information read here: https://developer.android.com/media/implement/surfaces/mobile

To customize it, put the example below inside your project folder.

## Example
```xml
<!-- YOUR_PROJECT_DIR/android/app/src/main/res/values/strings.xml -->
<resources>
    <!-- rtnp channel id -->
    <string name="rntp_temporary_channel_id">temporary_channel</string>
    <!-- rtnp channel name -->
    <string name="rntp_temporary_channel_name">temporary_channel</string>
    <!-- playback_channel_name used by KotlinAudio in rntp -->
    <string name="playback_channel_name">Music Player</string>
</resources>
```
