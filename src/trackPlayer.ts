import {
  AppRegistry,
  DeviceEventEmitter,
  NativeEventEmitter,
  NativeModules,
  Platform,
} from 'react-native';
// @ts-expect-error because resolveAssetSource is untyped
import { default as resolveAssetSource } from 'react-native/Libraries/Image/resolveAssetSource';
import type {
  EventPayloadByEvent,
  MetadataOptions,
  NowPlayingMetadata,
  PlayerOptions,
  ServiceHandler,
  Track,
  TrackMetadataBase,
} from './interfaces';
import { Event, RepeatMode, State } from './constants';

const { TrackPlayerModule: TrackPlayer } = NativeModules;
const emitter =
  Platform.OS !== 'android'
    ? new NativeEventEmitter(TrackPlayer)
    : DeviceEventEmitter;

// MARK: - Helpers
function resolveImportedPath(path?: number | string) {
  if (!path) return undefined;
  return resolveAssetSource(path) || path;
}

// MARK: - General API

/**
 * Initializes the player with the specified options.
 */
export async function setupPlayer(options: PlayerOptions = {}): Promise<void> {
  return TrackPlayer.setupPlayer(options || {});
}

/**
 * Register the playback service. The service will run as long as the player runs.
 */
export function registerPlaybackService(factory: () => ServiceHandler) {
  if (Platform.OS === 'android') {
    // Registers the headless task
    AppRegistry.registerHeadlessTask('TrackPlayer', factory);
  } else {
    // Initializes and runs the service in the next tick
    setImmediate(factory());
  }
}

export function addEventListener<T extends Event>(
  event: T,
  listener: EventPayloadByEvent[T] extends never
    ? () => void
    : (event: EventPayloadByEvent[T]) => void
) {
  return emitter.addListener(event, listener);
}

/**
 * @deprecated This method should not be used, most methods reject when service is not bound.
 */
export function isServiceRunning(): Promise<boolean> {
  return TrackPlayer.isServiceRunning();
}

// MARK: - Queue API

/**
 * Adds one or more tracks to the queue.
 */
export async function add(
  tracks: Track | Track[],
  insertBeforeIndex = -1
): Promise<number | void> {
  // Clone the array before modifying it
  if (Array.isArray(tracks)) {
    tracks = [...tracks];
  } else {
    tracks = [tracks];
  }

  if (tracks.length < 1) return;

  for (let i = 0; i < tracks.length; i++) {
    // Clone the object before modifying it
    tracks[i] = { ...tracks[i] };

    // Resolve the URLs
    tracks[i].url = resolveImportedPath(tracks[i].url);
    tracks[i].artwork = resolveImportedPath(tracks[i].artwork);
  }

  return TrackPlayer.add(tracks, insertBeforeIndex);
}

/**
 * Removes one or more tracks from the queue.
 */
export async function remove(tracks: number | number[]): Promise<void> {
  if (!Array.isArray(tracks)) {
    tracks = [tracks];
  }

  return TrackPlayer.remove(tracks);
}

/**
 * Clears any upcoming tracks from the queue.
 */
export async function removeUpcomingTracks(): Promise<void> {
  return TrackPlayer.removeUpcomingTracks();
}

/**
 * Skips to a track in the queue.
 */
export async function skip(
  trackIndex: number,
  initialPosition = -1
): Promise<void> {
  return TrackPlayer.skip(trackIndex, initialPosition);
}

/**
 * Skips to the next track in the queue.
 */
export async function skipToNext(initialPosition = -1): Promise<void> {
  return TrackPlayer.skipToNext(initialPosition);
}

/**
 * Skips to the previous track in the queue.
 */
export async function skipToPrevious(initialPosition = -1): Promise<void> {
  return TrackPlayer.skipToPrevious(initialPosition);
}

// MARK: - Control Center / Notifications API

/**
 * Updates the configuration for the components.
 */
export async function updateOptions(
  options: MetadataOptions = {}
): Promise<void> {
  options = { ...options };

  // Resolve the asset for each icon
  options.icon = resolveImportedPath(options.icon);
  options.playIcon = resolveImportedPath(options.playIcon);
  options.pauseIcon = resolveImportedPath(options.pauseIcon);
  options.stopIcon = resolveImportedPath(options.stopIcon);
  options.previousIcon = resolveImportedPath(options.previousIcon);
  options.nextIcon = resolveImportedPath(options.nextIcon);
  options.rewindIcon = resolveImportedPath(options.rewindIcon);
  options.forwardIcon = resolveImportedPath(options.forwardIcon);

  return TrackPlayer.updateOptions(options);
}

/**
 * Updates the metadata of a track in the queue. If the current track is updated,
 * the notification and the Now Playing Center will be updated accordingly.
 */
export async function updateMetadataForTrack(
  trackIndex: number,
  metadata: TrackMetadataBase
): Promise<void> {
  // Clone the object before modifying it
  metadata = Object.assign({}, metadata);

  // Resolve the artwork URL
  metadata.artwork = resolveImportedPath(metadata.artwork);

  return TrackPlayer.updateMetadataForTrack(trackIndex, metadata);
}

export function clearNowPlayingMetadata(): Promise<void> {
  return TrackPlayer.clearNowPlayingMetadata();
}

export function updateNowPlayingMetadata(
  metadata: NowPlayingMetadata
): Promise<void> {
  // Clone the object before modifying it
  metadata = Object.assign({}, metadata);

  // Resolve the artwork URL
  metadata.artwork = resolveImportedPath(metadata.artwork);

  return TrackPlayer.updateNowPlayingMetadata(metadata);
}

// MARK: - Player API

/**
 * Resets the player stopping the current track and clearing the queue.
 */
export async function reset(): Promise<void> {
  return TrackPlayer.reset();
}

/**
 * Plays or resumes the current track.
 */
export async function play(): Promise<void> {
  return TrackPlayer.play();
}

/**
 * Pauses the current track.
 */
export async function pause(): Promise<void> {
  return TrackPlayer.pause();
}

/**
 * Seeks to a specified time position in the current track.
 */
export async function seekTo(position: number): Promise<void> {
  return TrackPlayer.seekTo(position);
}

/**
 * Sets the volume of the player.
 */
export async function setVolume(level: number): Promise<void> {
  return TrackPlayer.setVolume(level);
}

/**
 * Sets the playback rate.
 */
export async function setRate(rate: number): Promise<void> {
  return TrackPlayer.setRate(rate);
}

/**
 * Sets the repeat mode.
 */
export async function setRepeatMode(mode: RepeatMode): Promise<RepeatMode> {
  return TrackPlayer.setRepeatMode(mode);
}

// MARK: - Getters

/**
 * Gets the volume of the player (a number between 0 and 1).
 */
export async function getVolume(): Promise<number> {
  return TrackPlayer.getVolume();
}

/**
 * Gets the playback rate, where 1 is the regular speed.
 */
export async function getRate(): Promise<number> {
  return TrackPlayer.getRate();
}

/**
 * Gets a track object from the queue.
 */
export async function getTrack(trackIndex: number): Promise<Track | null> {
  return TrackPlayer.getTrack(trackIndex);
}

/**
 * Gets the whole queue.
 */
export async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue();
}

/**
 * Gets the index of the current track.
 */
export async function getCurrentTrack(): Promise<number | null> {
  return TrackPlayer.getCurrentTrack();
}

/**
 * Gets the duration of the current track in seconds.
 */
export async function getDuration(): Promise<number> {
  return TrackPlayer.getDuration();
}

/**
 * Gets the buffered position of the current track in seconds.
 */
export async function getBufferedPosition(): Promise<number> {
  return TrackPlayer.getBufferedPosition();
}

/**
 * Gets the position of the current track in seconds.
 */
export async function getPosition(): Promise<number> {
  return TrackPlayer.getPosition();
}

/**
 * Gets the playback state of the player.
 */
export async function getState(): Promise<State> {
  return TrackPlayer.getState();
}

/**
 * Gets the repeat mode.
 */
export async function getRepeatMode(): Promise<RepeatMode> {
  return TrackPlayer.getRepeatMode();
}
