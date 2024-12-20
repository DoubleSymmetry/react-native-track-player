import {
  AppRegistry,
  DeviceEventEmitter,
  NativeEventEmitter,
  Platform
} from 'react-native';

import { AndroidAutoContentStyle, Event, RepeatMode } from './constants';
import type {
  AddTrack,
  AndroidAutoBrowseTree,
  EventPayloadByEvent,
  NowPlayingMetadata,
  PlaybackState,
  PlayerOptions,
  Progress,
  ServiceHandler,
  Track,
  TrackMetadataBase,
  UpdateOptions,
} from './interfaces';
import resolveAssetSource from './resolveAssetSource';
import TrackPlayer from './TrackPlayerModule';

const isAndroid = Platform.OS === 'android';

const emitter = !isAndroid
  ? new NativeEventEmitter(TrackPlayer)
  : DeviceEventEmitter;

// MARK: - Helpers

function resolveImportedAssetOrPath(pathOrAsset: string | number | undefined) {
  return pathOrAsset === undefined
    ? undefined
    : typeof pathOrAsset === 'string'
    ? pathOrAsset
    : resolveImportedAsset(pathOrAsset);
}

function resolveImportedAsset(id?: number) {
  return id
    ? (resolveAssetSource(id) as { uri: string } | null) ?? undefined
    : undefined;
}

// MARK: - General API

/**
 * Initializes the player with the specified options.
 *
 * @param options The options to initialize the player with.
 * @see https://rntp.dev/docs/api/functions/lifecycle
 */
export async function setupPlayer(
  options: PlayerOptions = {}
): Promise<void> {
  return TrackPlayer.setupPlayer(options)
}

/**
 * Register the playback service. The service will run as long as the player runs.
 */
export function registerPlaybackService(factory: () => ServiceHandler) {
  if (isAndroid) {
    // Registers the headless task
    AppRegistry.registerHeadlessTask('TrackPlayer', factory);
  } else if (Platform.OS === 'web') {
    factory()();
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

// MARK: - Queue API

/**
 * Adds one or more tracks to the queue.
 *
 * @param tracks The tracks to add to the queue.
 * @param insertBeforeIndex (Optional) The index to insert the tracks before.
 * By default the tracks will be added to the end of the queue.
 */
export async function add(
  tracks: AddTrack[],
  insertBeforeIndex?: number
): Promise<number | void>;
/**
 * Adds a track to the queue.
 *
 * @param track The track to add to the queue.
 * @param insertBeforeIndex (Optional) The index to insert the track before.
 * By default the track will be added to the end of the queue.
 */
export async function add(
  track: AddTrack,
  insertBeforeIndex?: number
): Promise<number | void>;
export async function add(
  tracks: AddTrack | AddTrack[],
  insertBeforeIndex = -1
): Promise<number | void> {
  const resolvedTracks = (Array.isArray(tracks) ? tracks : [tracks]).map(
    (track) => ({
      ...track,
      url: resolveImportedAssetOrPath(track.url),
      artwork: resolveImportedAssetOrPath(track.artwork),
    })
  );
  return resolvedTracks.length < 1
    ? undefined
    : TrackPlayer.add(resolvedTracks, insertBeforeIndex);
}

/**
 * Replaces the current track or loads the track as the first in the queue.
 *
 * @param track The track to load.
 */
export async function load(track: Track): Promise<number | void> {
  return TrackPlayer.load(track);
}

/**
 * Move a track within the queue.
 *
 * @param fromIndex The index of the track to be moved.
 * @param toIndex The index to move the track to. If the index is larger than
 * the size of the queue, then the track is moved to the end of the queue.
 */
export async function move(fromIndex: number, toIndex: number): Promise<void> {
  return TrackPlayer.move(fromIndex, toIndex);
}

/**
 * Removes multiple tracks from the queue by their indexes.
 *
 * If the current track is removed, the next track will activated. If the
 * current track was the last track in the queue, the first track will be
 * activated.
 *
 * @param indexes The indexes of the tracks to be removed.
 */
export async function remove(indexes: number[]): Promise<void>;
/**
 * Removes a track from the queue by its index.
 *
 * If the current track is removed, the next track will activated. If the
 * current track was the last track in the queue, the first track will be
 * activated.
 *
 * @param index The index of the track to be removed.
 */
export async function remove(index: number): Promise<void>;
export async function remove(indexOrIndexes: number | number[]): Promise<void> {
  return TrackPlayer.remove(
    Array.isArray(indexOrIndexes) ? indexOrIndexes : [indexOrIndexes]
  );
}

/**
 * Clears any upcoming tracks from the queue.
 */
export async function removeUpcomingTracks(): Promise<void> {
  return TrackPlayer.removeUpcomingTracks();
}

/**
 * Skips to a track in the queue.
 *
 * @param index The index of the track to skip to.
 * @param initialPosition (Optional) The initial position to seek to in seconds.
 */
export async function skip(index: number, initialPosition = -1): Promise<void> {
  return TrackPlayer.skip(index, initialPosition);
}

/**
 * Skips to the next track in the queue.
 *
 * @param initialPosition (Optional) The initial position to seek to in seconds.
 */
export async function skipToNext(initialPosition = -1): Promise<void> {
  return TrackPlayer.skipToNext(initialPosition);
}

/**
 * Skips to the previous track in the queue.
 *
 * @param initialPosition (Optional) The initial position to seek to in seconds.
 */
export async function skipToPrevious(initialPosition = -1): Promise<void> {
  return TrackPlayer.skipToPrevious(initialPosition);
}

// MARK: - Control Center / Notifications API

/**
 * Updates the configuration for the components.
 *
 * @param options The options to update.
 * @see https://rntp.dev/docs/api/functions/player#updateoptionsoptions
 */
export async function updateOptions(options: UpdateOptions = {}): Promise<void> {
  return TrackPlayer.updateOptions({
    ...options,
    android: {
      ...options.android,
    },
    icon: resolveImportedAsset(options.icon),
    playIcon: resolveImportedAsset(options.playIcon),
    pauseIcon: resolveImportedAsset(options.pauseIcon),
    stopIcon: resolveImportedAsset(options.stopIcon),
    previousIcon: resolveImportedAsset(options.previousIcon),
    nextIcon: resolveImportedAsset(options.nextIcon),
    rewindIcon: resolveImportedAsset(options.rewindIcon),
    forwardIcon: resolveImportedAsset(options.forwardIcon),
  });
}

/**
 * Updates the metadata of a track in the queue. If the current track is updated,
 * the notification and the Now Playing Center will be updated accordingly.
 *
 * @param trackIndex The index of the track whose metadata will be updated.
 * @param metadata The metadata to update.
 */
export async function updateMetadataForTrack(
  trackIndex: number,
  metadata: TrackMetadataBase
): Promise<void> {
  return TrackPlayer.updateMetadataForTrack(trackIndex, {
    ...metadata,
    artwork: resolveImportedAssetOrPath(metadata.artwork),
  });
}

/**
 * Updates the metadata content of the notification (Android) and the Now Playing Center (iOS)
 * without affecting the data stored for the current track.
 */
export function updateNowPlayingMetadata(
  metadata: NowPlayingMetadata
): Promise<void> {
  return TrackPlayer.updateNowPlayingMetadata({
    ...metadata,
    artwork: resolveImportedAssetOrPath(metadata.artwork),
  });
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
 * Stops the current track.
 */
export async function stop(): Promise<void> {
  return TrackPlayer.stop();
}

/**
 * Sets whether the player will play automatically when it is ready to do so.
 * This is the equivalent of calling `TrackPlayer.play()` when `playWhenReady = true`
 * or `TrackPlayer.pause()` when `playWhenReady = false`.
 */
export async function setPlayWhenReady(
  playWhenReady: boolean
): Promise<boolean> {
  return TrackPlayer.setPlayWhenReady(playWhenReady);
}

/**
 * Gets whether the player will play automatically when it is ready to do so.
 */
export async function getPlayWhenReady(): Promise<boolean> {
  return TrackPlayer.getPlayWhenReady();
}

/**
 * Seeks to a specified time position in the current track.
 *
 * @param position The position to seek to in seconds.
 */
export async function seekTo(position: number): Promise<void> {
  return TrackPlayer.seekTo(position);
}

/**
 * Seeks by a relative time offset in the current track.
 *
 * @param offset The time offset to seek by in seconds.
 */
export async function seekBy(offset: number): Promise<void> {
  return TrackPlayer.seekBy(offset);
}

/**
 * Sets the volume of the player.
 *
 * @param volume The volume as a number between 0 and 1.
 */
export async function setVolume(level: number): Promise<void> {
  return TrackPlayer.setVolume(level);
}

/**
 * Sets the playback rate.
 *
 * @param rate The playback rate to change to, where 0.5 would be half speed,
 * 1 would be regular speed, 2 would be double speed etc.
 */
export async function setRate(rate: number): Promise<void> {
  return TrackPlayer.setRate(rate);
}

/**
 * Sets the queue.
 *
 * @param tracks The tracks to set as the queue.
 * @see https://rntp.dev/docs/api/constants/repeat-mode
 */
export async function setQueue(tracks: Track[]): Promise<void> {
  return TrackPlayer.setQueue(tracks);
}

/**
 * Sets the queue repeat mode.
 *
 * @param repeatMode The repeat mode to set.
 * @see https://rntp.dev/docs/api/constants/repeat-mode
 */
export async function setRepeatMode(mode: RepeatMode): Promise<RepeatMode> {
  return TrackPlayer.setRepeatMode(mode);
}

// MARK: - Getters

/**
 * Gets the volume of the player as a number between 0 and 1.
 */
export async function getVolume(): Promise<number> {
  return TrackPlayer.getVolume();
}

/**
 * Gets the playback rate where 0.5 would be half speed, 1 would be
 * regular speed and 2 would be double speed etc.
 */
export async function getRate(): Promise<number> {
  return TrackPlayer.getRate();
}

/**
 * Gets a track object from the queue.
 *
 * @param index The index of the track.
 * @returns The track object or undefined if there isn't a track object at that
 * index.
 */
export async function getTrack(index: number): Promise<Track | undefined> {
  return TrackPlayer.getTrack(index);
}

/**
 * Gets the whole queue.
 */
export async function getQueue(): Promise<Track[]> {
  return TrackPlayer.getQueue();
}

/**
 * Gets the index of the active track in the queue or undefined if there is no
 * current track.
 */
export async function getActiveTrackIndex(): Promise<number | undefined> {
  return (await TrackPlayer.getActiveTrackIndex()) ?? undefined;
}

/**
 * Gets the active track or undefined if there is no current track.
 */
export async function getActiveTrack(): Promise<Track | undefined> {
  return (await TrackPlayer.getActiveTrack()) ?? undefined;
}

/**
 * Gets information on the progress of the currently active track, including its
 * current playback position in seconds, buffered position in seconds and
 * duration in seconds.
 */
export async function getProgress(): Promise<Progress> {
  return TrackPlayer.getProgress();
}

/**
 * Gets the playback state of the player.
 *
 * @see https://rntp.dev/docs/api/constants/state
 */
export async function getPlaybackState(): Promise<PlaybackState> {
  return TrackPlayer.getPlaybackState();
}

/**
 * Gets the queue repeat mode.
 *
 * @see https://rntp.dev/docs/api/constants/repeat-mode
 */
export async function getRepeatMode(): Promise<RepeatMode> {
  return TrackPlayer.getRepeatMode();
}

/**
 * Retries the current item when the playback state is `State.Error`.
 */
export async function retry() {
  return TrackPlayer.retry();
}

/**
 * Sets the content hierarchy of Android Auto (Android only). The hierarchy structure is a dict with
 * the mediaId as keys, and a list of MediaItem as values. To use, you must at least specify the root directory, where
 * the key is "/". If the root directory contains BROWSABLE MediaItems, they will be shown as tabs. Do note Google requires
 * AA to have a max of 4 tabs. You may then set the mediaId keys of the browsable MediaItems to be a list of other MediaItems.
 *
 * @param browseTree the content hierarchy dict.
 * @returns a serialized copy of the browseTree set by native. For debug purposes.
 */
export async function setBrowseTree(
  browseTree: AndroidAutoBrowseTree
): Promise<string> {
  if (!isAndroid) return new Promise(() => '');
  return TrackPlayer.setBrowseTree(browseTree);
}

/**
 * this method enables android auto playback progress tracking; see
 * https://developer.android.com/training/cars/media#browse-progress-bar
 * android only.
 * @param mediaID the mediaID.
 * @returns
 */
export async function setPlaybackState(mediaID: string): Promise<void> {
  if (!isAndroid) return;
  TrackPlayer.setPlaybackState(mediaID);
}

/**
 * Sets the content style of Android Auto (Android only).
 * there are list style and grid style. see https://developer.android.com/training/cars/media#apply_content_style .
 * the styles are applicable to browsable nodes and playable nodes. setting the args to true will yield the list style.
 * false = the grid style.
 */
export function setBrowseTreeStyle(
  browsableStyle: AndroidAutoContentStyle,
  playableStyle: AndroidAutoContentStyle
): null {
  if (!isAndroid) return null;
  TrackPlayer.setBrowseTreeStyle(browsableStyle, playableStyle);
  return null;
}

/**
 * acquires the wake lock of MusicService (android only.)
 */
export async function acquireWakeLock() {
  if (!isAndroid) return;
  TrackPlayer.acquireWakeLock();
}

/**
 * acquires the wake lock of MusicService (android only.)
 */
export async function abandonWakeLock() {
  if (!isAndroid) return;
  TrackPlayer.abandonWakeLock();
}

/**
 * get onStartCommandIntent is null or not (Android only.). this is used to identify
 * if musicservice is restarted or not.
 */
export async function validateOnStartCommandIntent(): Promise<boolean> {
  if (!isAndroid) return true;
  return TrackPlayer.validateOnStartCommandIntent();
}
