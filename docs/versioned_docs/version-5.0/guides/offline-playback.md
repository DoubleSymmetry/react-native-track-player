---
sidebar_position: 1
---

# Offline Playback

There are two general use-cases for offline playback:

1. An "Offline Only" case where all the audio is bundled with your App itself.
2. A "Hybrid Offline/Network" case where some of the time you're playing from a
  network and sometime you're playing offline.

Both of these can be achieved by with this project. The only practical
difference between the two is in the 2nd you'll need another package to
download your audio while your App is running instead of loading into the App's
source at build time.

After that, you simply send a `Track` object to the player with a **local file
path** to your audio.

## Offline Only

This case is simple, just stick your audio files in your repository with your
source code and use the file paths to them when adding Tracks.

⚠️ Please take into consideration that this approach will increase
the size of your App based on how much audio you want the user to be able to
play. If you're doing anything substantial, it's recommended that you use
the [Hybrid Offline/Network](#hybrid-offline-network) approach.

## Hybrid Offline/Network

To do this you'll first need to install a package like:

- [react-native-fs](https://github.com/itinance/react-native-fs/)
- [rn-fetch-blob](https://github.com/joltup/rn-fetch-blob)
- [expo-file-system](https://www.npmjs.com/package/expo-file-system)

The typical approach is to then create a download button in your app, which,
once clicked, uses one of the above packages to download your audio to a local
file. Then voila! Simply play the local file after download.
