---
sidebar_position: 4
---

# Multitrack Progress

If you're building an app that allows the playback of more than one Track you'll
probably also want to keep track of and display the users progress for each of
those tracks. **RNTP does not handle this for you**, but offers everything you
need in order to build it yourself.

## The Wrong Way

The most common misconception is that one could simply create a list of tracks
and then simply call `useProgress` in each of them to get their progress.
However, this doesn't work, as **`useProgress` is _only_ concerned with the
progress of the currently playing track!** If you attempt to do it this way
you'll quickly realize that all of your tracks are showing the exact same
progress, which given the understanding of `useProgress` above, should make
perfect sense.

The other problem with this approach is that when a user listens headlessly (
or when the player is in the background), you won't get any progress updates.

## The Right Way

You're responsible for storing your progress on each track outside of RNTP, and
then using that progress when displaying things to your users. At a high-level,
what you need to do is store a record somewhere that associates a progress with
a unique track. Let's say we want to store a record that has a `track.id` and a
`track.progress`. Then what we want to do is _periodically_ update this record
while a given track is playing. Finally, when you want to display or otherwise
use your progress you should _read_ from the stored record (not from RNTP). See
the example below where we're going to use
[zustand](https://www.npmjs.com/package/zustand). Zustand will allow us to store
(and with some additional configuration, persist) our track progress AND it
gives us a nice way to dynamically update our progress displays in
realtime/reactively.

Please note, that the below solution assumes that you're adding an `id` property
to your `Track` object before you add it to RNTP, as RNTP does not add `id`'s
to your tracks by default, nor does it require them.

#### 1. Setup Zustand

First let's create a basic zustand store to store our progress in:

```ts
// src/store.ts
import create from 'zustand';
import type { SetState } from 'zustand/vanilla';

type ProgressStateStore = {
  map: Record<string, number>;
  setProgress: (id: string, progress: number) => void;
};

export const useProgressStateStore = create<ProgressStateStore>(
  (set: SetState<ProgressStateStore>) => ({
    map: {},
    setProgress: (id: string, progress: number) => set((state) => {
      state.map[id] = progress;
    }),
  })
);
```

Let's also set up a little helper hook to make it easier to read progress (we'll
use this later on):

```ts
// src/hooks/useTrackProgress.ts
import { useCallback } from 'react';
import { useProgressStateStore } from '../store';

export const useTrackProgress = (id: string | number): number => {
  return useProgressStateStore(useCallback(state => {
    return state.map[id.toString()] || 0;
  }, [id]));
};
```

#### 2. Listen To Progress Updates

Next we need to set up a listener for progress updates in our
[playback service](../basics/playback-service.md) and update our zustand store:

```ts
// src/services/PlaybackService.ts
import TrackPlayer, { Event } from 'react-native-track-player';
import { useProgressStateStore } from '../store';

// create a local reference for the `setProgress` function
const setProgress = useProgressStateStore.getState().setProgress;

export const PlaybackService = async function() {
  TrackPlayer.addEventListener(Event.PlaybackProgressUpdated, async ({ position, track }) => {
    // get the track to fetch your unique ID property (if applicable)
    const track = await TrackPlayer.getTrack(track);
    // write progress to the zustand store
    setProgress(track.id, position);
  });
};
```

⚠️ make sure you've configured your `progressUpdateEventInterval`
in the `TrackPlayer.updateOptions` call.


#### 3. Reactively Update Progress

Finally, we just need to read from the store whenever we display our track list
item:

```ts
// src/components/TrackListItem.tsx
import type { Track } from 'react-native-track-player';
import { useTrackProgress } from '../hooks/useTrackProgress';

export interface TrackListItemProps {}

export const TrackListItem: React.FC<TrackListItemProps> = (track: Track) => {
  const progress = useTrackProgress(track.id);
  return (
    <Text>Progress: {progress}</Text>
  );
};
```

:confetti_ball: voilà
