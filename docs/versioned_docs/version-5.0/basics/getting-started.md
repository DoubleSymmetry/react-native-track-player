---
sidebar_position: 2
---

# Getting Started

## Starting off
First, you need to register a [playback service](./playback-service.md) right after registering the main component of your app (typically in your `index.js` file at the root of your project):
```ts
// AppRegistry.registerComponent(...);
TrackPlayer.registerPlaybackService(() => require('./service'));
```

```ts
// service.js
module.exports = async function() {
    // This service needs to be registered for the module to work
    // but it will be used later in the "Receiving Events" section
}
```

Then, you need to set up the player. This usually takes less than a second:
```ts
import TrackPlayer from 'react-native-track-player';

await TrackPlayer.setupPlayer()
// The player is ready to be used
```

Make sure the setup method has completed before interacting with any other functions in `TrackPlayer` in order to avoid instability.

## Controlling the Player

### Adding Tracks to the Playback Queue

You can add a track to the player using a url or by requiring a file in the app
bundle or on the file system.

First of all, you need to create a [track object](../api/objects/track.md), which
is a plain javascript object with a number of properties describing the track.
Then add the track to the queue:

```ts
var track1 = {
    url: 'http://example.com/avaritia.mp3', // Load media from the network
    title: 'Avaritia',
    artist: 'deadmau5',
    album: 'while(1<2)',
    genre: 'Progressive House, Electro House',
    date: '2014-05-20T07:00:00+00:00', // RFC 3339
    artwork: 'http://example.com/cover.png', // Load artwork from the network
    duration: 402 // Duration in seconds
};

const track2 = {
    url: require('./coelacanth.ogg'), // Load media from the app bundle
    title: 'Coelacanth I',
    artist: 'deadmau5',
    artwork: require('./cover.jpg'), // Load artwork from the app bundle
    duration: 166
};

const track3 = {
    url: 'file:///storage/sdcard0/Downloads/artwork.png', // Load media from the file system
    title: 'Ice Age',
    artist: 'deadmau5',
     // Load artwork from the file system:
    artwork: 'file:///storage/sdcard0/Downloads/cover.png',
    duration: 411
};

// You can then [add](https://rntp.dev/docs/api/functions/queue#addtracks-insertbeforeindex) the items to the queue
await TrackPlayer.add([track1, track2, track3]);
```

### Player Information

```ts

import TrackPlayer, { State } from 'react-native-track-player';

const state = await TrackPlayer.getState();
if (state === State.Playing) {
    console.log('The player is playing');
};

let trackIndex = await TrackPlayer.getCurrentTrack();
let trackObject = await TrackPlayer.getTrack(trackIndex);
console.log(`Title: ${trackObject.title}`);

const position = await TrackPlayer.getPosition();
const duration = await TrackPlayer.getDuration();
console.log(`${duration - position} seconds left.`);
```

### Changing Playback State

```ts
TrackPlayer.play();
TrackPlayer.pause();
TrackPlayer.reset();

// Seek to 12.5 seconds:
TrackPlayer.seekTo(12.5);

// Set volume to 50%:
TrackPlayer.setVolume(0.5);
```

### Controlling the Queue
```ts
// Skip to a specific track index:
await TrackPlayer.skip(trackIndex);

// Skip to the next track in the queue:
await TrackPlayer.skipToNext();

// Skip to the previous track in the queue:
await TrackPlayer.skipToPrevious();

// Remove two tracks from the queue:
await TrackPlayer.remove([trackIndex1, trackIndex2]);

// Retrieve the track objects in the queue:
const tracks = await TrackPlayer.getQueue();
console.log(`First title: ${tracks[0].title}`);
```
#### Playback Events

You can subscribe to [player events](../api/events.md#player), which describe the
changing nature of the playback state. For example, subscribe to the
`Event.PlaybackTrackChanged` event to be notified when the track has changed or
subscribe to the `Event.PlaybackState` event to be notified when the player
buffers, plays, pauses and stops.

#### Example
```tsx
import TrackPlayer, { Event } from 'react-native-track-player';

const PlayerInfo = () => {
    const [trackTitle, setTrackTitle] = useState<string>();

    // do initial setup, set initial trackTitle..

    useTrackPlayerEvents([Event.PlaybackTrackChanged], async event => {
        if (event.type === Event.PlaybackTrackChanged && event.nextTrack != null) {
            const track = await TrackPlayer.getTrack(event.nextTrack);
            const {title} = track || {};
            setTrackTitle(title);
        }
    });

    return (
        <Text>{trackTitle}</Text>
    );
}
```

## Progress Updates

Music apps often need an automated way to show the progress of a playing track.
For this purpose, we created [the hook: `useProgress`](../api/hooks.md) which
updates itself automatically.

#### Example

```tsx
import TrackPlayer, { useProgress } from 'react-native-track-player';

const MyPlayerBar = () => {
    const progress = useProgress();

    return (
            // Note: formatTime and ProgressBar are just examples:
            <View>
                <Text>{formatTime(progress.position)}</Text>
                <ProgressBar
                    progress={progress.position}
                    buffered={progress.buffered}
                />
            </View>
        );

}
```

## Track Player Options

Track Player can be configured using a number of options. Some of these options
pertain to the media controls available in the lockscreen / notification and how
they behave, others describe the availability of capabilities needed for
platform specific functionalities like Android Auto.

You can change options multiple times. You do not need to specify all the
options, just the ones you want to change.

For more information about the properties you can set, [check the
documentation](../api/functions/player.md#updateoptionsoptions).

#### Example

```ts
import TrackPlayer, { Capability } from 'react-native-track-player';

TrackPlayer.updateOptions({
    // Media controls capabilities
    capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
        Capability.Stop,
    ],

    // Capabilities that will show up when the notification is in the compact form on Android
    compactCapabilities: [Capability.Play, Capability.Pause],

    // Icons for the notification on Android (if you don't like the default ones)
    playIcon: require('./play-icon.png'),
    pauseIcon: require('./pause-icon.png'),
    stopIcon: require('./stop-icon.png'),
    previousIcon: require('./previous-icon.png'),
    nextIcon: require('./next-icon.png'),
    icon: require('./notification-icon.png')
});
```
