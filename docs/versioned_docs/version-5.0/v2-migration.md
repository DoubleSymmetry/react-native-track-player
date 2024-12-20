---
sidebar_position: 5
---

# Migrating from v1 to v2

All queue methods have been updating to work on indexes instead of id's. We want this library to support all kinds of apps -- and moving to be index based will allow us to better support applications who have long/endless queues and in the future to allow us to build a performant API around queue management.

We recommend using Typescript to have the system alert you of issues.

When migrating from v1 to v2, the following has changed:

```diff
// Methods

- async function add(tracks: Track | Track[], insertBeforeId?: string): Promise<void> {
+ async function add(tracks: Track | Track[], insertBeforeIndex?: number): Promise<void> {

- async function remove(tracks: string | string[]): Promise<void> {
+ async function remove(tracks: number | number[]): Promise<void> {

- async function skip(trackId: string): Promise<void> {
+ function skip(trackIndex: number): Promise<void> {

- async function updateMetadataForTrack(trackId: string, metadata: TrackMetadataBase): Promise<void> {
+ async function updateMetadataForTrack(trackIndex: number, metadata: TrackMetadataBase): Promise<void> {

- async function getTrack(trackId: string): Promise<Track> {
+ async function getTrack(trackIndex: number): Promise<Track> {

- async function getCurrentTrack(): Promise<string> {
+ async function getCurrentTrack(): Promise<number> {

// Imports

import TrackPlayer, {
-  STATE_XXX,
-  CAPABILITY_XXX,
-  PITCH_ALGORITHM_XXX,
-  RATING_XXX,
+  State,
+  Capability,
+  PitchAlgorithm,
+  RatingType,
+  Event,
+  RepeatMode
} from 'react-native-track-player'

// Hooks

- useTrackPlayerProgress
+ useProgress

// Event Listeners
// Refrain from using: TrackPlayer.addEventListener() and instead use the provided hooks

+ usePlaybackState
+ useTrackPlayerEvents
+ useProgress
```
