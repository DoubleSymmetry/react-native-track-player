import { DeviceEventEmitter } from 'react-native';

import type { Track, UpdateOptions } from '../src';
import { Event, PlaybackState, State } from '../src';
import { PlaylistPlayer, RepeatMode } from './TrackPlayer';
import { SetupNotCalledError } from './TrackPlayer/SetupNotCalledError';

export class TrackPlayerModule extends PlaylistPlayer {
  protected emitter = DeviceEventEmitter;
  protected progressUpdateEventInterval: any;

  // Capabilities
  public readonly CAPABILITY_PLAY = 'CAPABILITY_PLAY';
  public readonly CAPABILITY_PLAY_FROM_ID = 'CAPABILITY_PLAY_FROM_ID';
  public readonly CAPABILITY_PLAY_FROM_SEARCH = 'CAPABILITY_PLAY_FROM_SEARCH';
  public readonly CAPABILITY_PAUSE = 'CAPABILITY_PAUSE';
  public readonly CAPABILITY_STOP = 'CAPABILITY_STOP';
  public readonly CAPABILITY_SEEK_TO = 'CAPABILITY_SEEK_TO';
  public readonly CAPABILITY_SKIP = 'CAPABILITY_SKIP';
  public readonly CAPABILITY_SKIP_TO_NEXT = 'CAPABILITY_SKIP_TO_NEXT';
  public readonly CAPABILITY_SKIP_TO_PREVIOUS = 'CAPABILITY_SKIP_TO_PREVIOUS';
  public readonly CAPABILITY_JUMP_FORWARD = 'CAPABILITY_JUMP_FORWARD';
  public readonly CAPABILITY_JUMP_BACKWARD = 'CAPABILITY_JUMP_BACKWARD';
  public readonly CAPABILITY_SET_RATING = 'CAPABILITY_SET_RATING';
  public readonly CAPABILITY_LIKE = 'CAPABILITY_LIKE';
  public readonly CAPABILITY_DISLIKE = 'CAPABILITY_DISLIKE';
  public readonly CAPABILITY_BOOKMARK = 'CAPABILITY_BOOKMARK';

  // States
  public readonly STATE_NONE = 'STATE_NONE';
  public readonly STATE_READY = 'STATE_READY';
  public readonly STATE_PLAYING = 'STATE_PLAYING';
  public readonly STATE_PAUSED = 'STATE_PAUSED';
  public readonly STATE_STOPPED = 'STATE_STOPPED';
  public readonly STATE_BUFFERING = 'STATE_BUFFERING';
  public readonly STATE_CONNECTING = 'STATE_CONNECTING';

  // Rating Types
  public readonly RATING_HEART = 'RATING_HEART';
  public readonly RATING_THUMBS_UP_DOWN = 'RATING_THUMBS_UP_DOWN';
  public readonly RATING_3_STARS = 'RATING_3_STARS';
  public readonly RATING_4_STARS = 'RATING_4_STARS';
  public readonly RATING_5_STARS = 'RATING_5_STARS';
  public readonly RATING_PERCENTAGE = 'RATING_PERCENTAGE';

  // Repeat Modes
  public readonly REPEAT_OFF = RepeatMode.Off;
  public readonly REPEAT_TRACK = RepeatMode.Track;
  public readonly REPEAT_QUEUE = RepeatMode.Playlist;

  // Pitch Algorithms
  public readonly PITCH_ALGORITHM_LINEAR = 'PITCH_ALGORITHM_LINEAR';
  public readonly PITCH_ALGORITHM_MUSIC = 'PITCH_ALGORITHM_MUSIC';
  public readonly PITCH_ALGORITHM_VOICE = 'PITCH_ALGORITHM_VOICE';

  // observe and emit state changes
  public get state(): PlaybackState {
    return super.state;
  }
  public set state(newState: PlaybackState) {
    super.state = newState;
    this.emitter.emit(Event.PlaybackState, newState);
  }

  public async updateOptions(options: UpdateOptions) {
    this.setupProgressUpdates(options.progressUpdateEventInterval);
  }

  protected setupProgressUpdates(interval?: number) {
    // clear and reset interval
    this.clearUpdateEventInterval();
    if (interval) {
      this.clearUpdateEventInterval()
      this.progressUpdateEventInterval = setInterval(
        async () => {
          if (this.state.state === State.Playing) {
            const progress = await this.getProgress()
            this.emitter.emit(Event.PlaybackProgressUpdated, {
              ...progress,
              track: this.currentIndex,
            });
          }
        },
        interval * 1000,
      )
    }
  }

  protected clearUpdateEventInterval() {
    if (this.progressUpdateEventInterval) {
      clearInterval(this.progressUpdateEventInterval);
    }
  }

  protected async onPlaylistEnded() {
    await super.onPlaylistEnded();
    this.emitter.emit(Event.PlaybackQueueEnded, {
      track: this.currentIndex,
      position: this.element!.currentTime,
    });
  }

  public get playWhenReady(): boolean {
    return super.playWhenReady;
  }

  public set playWhenReady(pwr: boolean) {
    const didChange = pwr !== this._playWhenReady;
    super.playWhenReady = pwr;

    if (didChange) {
      this.emitter.emit(Event.PlaybackPlayWhenReadyChanged, { playWhenReady: this._playWhenReady });
    }
  }

  public getPlayWhenReady(): boolean {
    return this.playWhenReady;
  }

  public setPlayWhenReady(pwr: boolean): boolean {
    this.playWhenReady = pwr;
    return this.playWhenReady;
  }

  public async load(track: Track) {
    if (!this.element) throw new SetupNotCalledError();
    const lastTrack = this.current;
    const lastPosition = this.element.currentTime;
    await super.load(track);

    this.emitter.emit(Event.PlaybackActiveTrackChanged, {
      lastTrack,
      lastPosition,
      lastIndex: this.lastIndex,
      index: this.currentIndex,
      track,
    });
  }

  public getQueue(): Track[] {
    return this.playlist;
  }

  public async setQueue(queue: Track[]) {
    await this.stop();
    this.playlist = queue;
  }

  public getActiveTrack(): Track | undefined {
    return this.current;
  }

  public getActiveTrackIndex(): number | undefined {
    // per the existing spec, this should throw if setup hasn't been called
    if (!this.element || !this.player) throw new SetupNotCalledError();
    return this.currentIndex;
  }

  public getPlaybackState(): PlaybackState {
    return this.state;
  }
}
