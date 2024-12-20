import type { ResourceObject } from './ResourceObject';
import { MediaItemPlayable } from '../constants';

export interface MediaItem {
  mediaId: string;
  title: string;
  subtitle?: string;
  /**mediaUri doesn't seem to work. use Event.RemotePlayId to handle playback instead. */
  mediaUri?: string | ResourceObject;
  iconUri?: string | ResourceObject;
  /** playable has 2 states: 1 as browsable, or any other as playable.  */
  playable: MediaItemPlayable.MediaBrowsable | string;
  groupTitle?: string;
  contentStyle?: string;
  childrenPlayableContentStyle?: string;
  childrenBrowsableContentStyle?: string;
  /** playbackProgress should contain a string representation of a number between 0 and 1 if present */
  playbackProgress?: string;
}
