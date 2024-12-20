export enum State {
  /** Indicates that no media is currently loaded */
  None = 'none',
  /** Indicates that the player is paused, but ready to start playing */
  Ready = 'ready',
  /** Indicates that the player is currently playing */
  Playing = 'playing',
  /** Indicates that the player is currently paused */
  Paused = 'paused',
  /** Indicates that the player is currently stopped */
  Stopped = 'stopped',
  /** Indicates that the initial load of the item is occurring. */
  Loading = 'loading',
  /**
   * Indicates that the player is currently loading more data before it can
   * continue playing or is ready to start playing.
   */
  Buffering = 'buffering',
  /**
   * Indicates that playback of the current item failed. Call `TrackPlayer.getError()`
   * to get more information on the type of error that occurred.
   * Call `TrackPlayer.retry()` or `TrackPlayer.play()` to try to play the item
   * again.
   */
  Error = 'error',
  /**
   * Indicates that playback stopped due to the end of the queue being reached.
   */
  Ended = 'ended',
}
