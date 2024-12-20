---
sidebar_position: 6
---

# iOS Category (ios-only)

All iOS Category types are made available through the named export `IOSCategory`:

```ts
import { IOSCategory } from 'react-native-track-player';
```

## `Playback`

The category for playing recorded music or other sounds that are central to the
successful use of your app.

[See the Apple Docs](https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616509-playback)


## `PlayAndRecord`

The category for recording (input) and playback (output) of audio, such as for a
Voice over Internet Protocol (VoIP) app.

[See the Apple Docs](https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616568-playandrecord)

## `MultiRoute`

The category for routing distinct streams of audio data to different output
devices at the same time.

[See the Apple Docs](https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616484-multiroute) 
  
## `Ambient`

The category for an app in which sound playback is nonprimary — that is, your
app also works with the sound turned off.

[See the Apple Docs](https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616560-ambient)

## `SoloAmbient`

The default audio session category.

[See the Apple Docs](https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616488-soloambient)

## `Record`

The category for recording audio while also silencing playback audio.

[See the Apple Docs](https://developer.apple.com/documentation/avfaudio/avaudiosession/category/1616451-record)
