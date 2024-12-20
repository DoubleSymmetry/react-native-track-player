import * as TrackPlayer from '../trackPlayer';
import { State } from '../constants';
import { usePlayWhenReady } from './usePlayWhenReady';
import { usePlaybackState } from './usePlaybackState';

/**
 * Tells whether the TrackPlayer is in a mode that most people would describe
 * as "playing." Great for UI to decide whether to show a Play or Pause button.
 * @returns playing - whether UI should likely show as Playing, or undefined
 *   if this isn't yet known.
 * @returns bufferingDuringPlay - whether UI should show as Buffering, or
 *   undefined if this isn't yet known.
 */
export function useIsPlaying() {
  const state = usePlaybackState().state;
  const playWhenReady = usePlayWhenReady();

  return determineIsPlaying(playWhenReady, state);
}

function determineIsPlaying(playWhenReady?: boolean, state?: State) {
  if (playWhenReady === undefined || state === undefined) {
    return { playing: undefined, bufferingDuringPlay: undefined };
  }

  const isLoading = state === State.Loading || state === State.Buffering;
  const isErrored = state === State.Error;
  const isEnded = state === State.Ended;
  const isNone = state === State.None;

  return {
    playing: playWhenReady && !(isErrored || isEnded || isNone),
    bufferingDuringPlay: playWhenReady && isLoading,
  };
}

/**
 * This exists if you need realtime status on whether the TrackPlayer is
 * playing, whereas the hooks all have a delay because they depend on responding
 * to events before their state is updated.
 *
 * It also exists whenever you need to know the play state outside of a React
 * component, since hooks only work in components.
 *
 * @returns playing - whether UI should likely show as Playing, or undefined
 *   if this isn't yet known.
 * @returns bufferingDuringPlay - whether UI should show as Buffering, or
 *   undefined if this isn't yet known.
 */
export async function isPlaying() {
  const [playbackState, playWhenReady] = await Promise.all([
    TrackPlayer.getPlaybackState(),
    TrackPlayer.getPlayWhenReady(),
  ]);
  return determineIsPlaying(playWhenReady, playbackState.state);
}
