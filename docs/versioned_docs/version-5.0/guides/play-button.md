---
sidebar_position: 4
---

# Play Buttons

UI often needs to display a Play button that changes between three states:

1. Play
2. Pause
3. Spinner (e.g. if playback is being attempted, but sound is paused due to buffering)

Implementing this correctly will take a bit of care. For instance, `usePlaybackState` can return `State.Buffering` even if playback is currently paused. `usePlayWhenReady` is one way to check if the player is attempting to play, but can return true even if `PlaybackState` is `State.Error` or `State.Ended`.

To determine how to render a Play button in its three states correctly, do the following:

* Render the button as a spinner if `playWhenReady` and `state === State.Loading || state === State.Buffering`
* Else render the button as being in the Playing state if `playWhenReady && !(state === State.Error || state === State.Buffering)`
* Otherwise render the button as being in the Paused state

To help with this logic, the API has two utilities:

1. The `useIsPlaying()` hook. This returns `{playing: boolean | undefined, bufferingDuringPlay: boolean | undefined}`, which you can consult to render your play button correctly. You should render a spinner if `bufferingDuringPlay === true`; otherwise render according to `playing`. Values are `undefined` if the player isn't yet in a state where they can be determined.
2. The `async isPlaying()` function, which returns the same result as `useIsPlaying()`, but can be used outside of React components (i.e. without hooks). Note that you can't easily just instead call `getPlaybackState()` to determine the same answer, unless you've accounted for the issues mentioned above.