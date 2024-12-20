import { Player } from './Player';

import { State } from '../../src';
import type { Track } from '../../src/interfaces';
import { RepeatMode } from './RepeatMode';

export class PlaylistPlayer extends Player {
  // TODO: use immer to make the `playlist` immutable
  protected playlist: Track[] = [];
  protected lastIndex?: number;
  protected _currentIndex?: number;
  protected repeatMode: RepeatMode = RepeatMode.Off;

  protected async onStateUpdate(state: Exclude<State, State.Error>) {
    super.onStateUpdate(state);

    if (state === State.Ended) {
      await this.onTrackEnded();
    }
  }

  protected async onTrackEnded() {
    switch (this.repeatMode) {
      case RepeatMode.Track:
        if (this.currentIndex !== undefined) {
          await this.goToIndex(this.currentIndex);
        }
        break;
      case RepeatMode.Playlist:
        if (this.currentIndex === this.playlist.length - 1) {
          await this.goToIndex(0);
        } else {
          await this.skipToNext();
        }
        break;
      default:
        try {
          await this.skipToNext();
        } catch (err) {
          if ((err as Error).message !== 'playlist_exhausted') {
            throw err;
          }
          this.onPlaylistEnded();
        }
        break;
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  protected onPlaylistEnded() {}

  protected get currentIndex() {
    return this._currentIndex;
  }

  protected set currentIndex(current: number | undefined) {
    this.lastIndex = this.currentIndex;
    this._currentIndex = current;
  }

  protected async goToIndex(index: number, initialPosition?: number) {
    const track = this.playlist[index];

    if (!track) {
      throw new Error('playlist_exhausted');
    }

    if (this.currentIndex !== index) {
      this.currentIndex = index;
      await this.load(track);
    }

    if (initialPosition) {
      this.seekTo(initialPosition);
    }

    if (this.playWhenReady) {
      await this.play();
    }
  }

  public async add(tracks: Track[], insertBeforeIndex?: number) {
    if (insertBeforeIndex !== -1 && insertBeforeIndex !== undefined) {
      this.playlist.splice(insertBeforeIndex, 0, ...tracks);
    } else {
      this.playlist.push(...tracks);
    }

    if (this.currentIndex === undefined) {
      await this.goToIndex(0);
    }
  }

  public async skip(index: number, initialPosition?: number) {
    const track = this.playlist[index];

    if (track === undefined) {
      throw new Error('index out of bounds');
    }

    await this.goToIndex(index, initialPosition);
  }

  public async skipToNext(initialPosition?: number) {
    if (this.currentIndex === undefined) return;

    const index = this.currentIndex + 1;
    await this.goToIndex(index, initialPosition);
  }

  public async skipToPrevious(initialPosition?: number) {
    if (this.currentIndex === undefined) return;

    const index = this.currentIndex - 1;
    await this.goToIndex(index, initialPosition);
  }

  public getTrack(index: number): Track | null {
    const track = this.playlist[index];
    return track || null;
  }

  public setRepeatMode(mode: RepeatMode) {
    this.repeatMode = mode;
  }

  public getRepeatMode() {
    return this.repeatMode;
  }

  public async remove(indexes: number[]) {
    const idxMap = indexes.reduce<Record<number, boolean>>((acc, elem) => {
      acc[elem] = true
      return acc;
    }, {});
    let isCurrentRemoved = false;
    this.playlist = this.playlist.filter((_track, idx) => {
      const keep = !idxMap[idx]

      if (!keep && idx === this.currentIndex) {
        isCurrentRemoved = true;
      }

      return keep;
    });

    if (this.currentIndex === undefined) {
      return;
    }

    const hasItems = this.playlist.length > 0;
    if (isCurrentRemoved && hasItems) {
      await this.goToIndex(this.currentIndex % this.playlist.length);
    } else if (isCurrentRemoved) {
      await this.stop();
    }
  }

  public async stop() {
    await super.stop();
    this.currentIndex = undefined;
  }

  public async reset() {
    await this.stop();
    this.playlist = [];
  }

  public async removeUpcomingTracks() {
    if (this.currentIndex === undefined) return;
    this.playlist = this.playlist.slice(0, this.currentIndex + 1);
  }

  public async move(fromIndex: number, toIndex: number): Promise<void> {
    if (!this.playlist[fromIndex]) {
      throw new Error('index out of bounds');
    }

    if (this.currentIndex === fromIndex) {
      throw new Error('you cannot move the currently playing track');
    }

    if (this.currentIndex === toIndex) {
      throw new Error('you cannot replace the currently playing track');
    }

    // calculate `currentIndex` after move
    let shift: number | undefined = undefined;
    if (this.currentIndex) {
      if (fromIndex < this.currentIndex && toIndex > this.currentIndex) {
        shift = -1;
      } else if (fromIndex > this.currentIndex && toIndex < this.currentIndex) {
        shift = + 1;
      }
    }

    // move the track
    const fromItem = this.playlist[fromIndex];
    this.playlist.splice(fromIndex, 1);
    this.playlist.splice(toIndex, 0, fromItem);

    if (this.currentIndex && shift) {
      this.currentIndex = this.currentIndex + shift
    }
  }

  // TODO
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  public updateMetadataForTrack(index: number, metadata: Partial<Track>) {}
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  public updateNowPlayingMetadata(metadata: Partial<Track>) {}

}
