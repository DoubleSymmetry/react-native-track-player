import { MediaItem } from './MediaItem';

export interface AndroidAutoBrowseTree {
  '/': MediaItem[];
  [key: string]: MediaItem[];
}
