---
sidebar_position: 8
---

# iOS Category Options (ios-only)

All iOS Category Options types are made available through the named export `IOSCategoryOptions`:

```ts
import { IOSCategoryOptions } from 'react-native-track-player';
```

## `MixWithOthers`

An option that indicates whether audio from this session mixes with audio
from active sessions in other audio apps.

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1616611-mixwithothers)

## `DuckOthers`

An option that reduces the volume of other audio sessions while audio from
this session plays.

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1616618-duckothers)

## `InterruptSpokenAudioAndMixWithOthers`

An option that determines whether to pause spoken audio content from other
sessions when your app plays its audio.

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1616534-interruptspokenaudioandmixwithot)

## `AllowBluetooth`

An option that determines whether Bluetooth hands-free devices appear as
available input routes.

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1616518-allowbluetooth)

## `AllowBluetoothA2DP`

An option that determines whether you can stream audio from this session
to Bluetooth devices that support the Advanced Audio Distribution Profile (A2DP).

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1771735-allowbluetootha2dp)

## `AllowAirPlay`

An option that determines whether you can stream audio from this session
to AirPlay devices.

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1771736-allowairplay)

## `DefaultToSpeaker`

An option that determines whether audio from the session defaults to the
built-in speaker instead of the receiver.

[See the Apple Docs ](https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions/1616462-defaulttospeaker)
