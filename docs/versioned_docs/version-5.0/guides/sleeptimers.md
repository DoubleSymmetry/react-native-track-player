---
sidebar_position: 3
---

# Sleeptimers

This guide has very similar principles and implementation to
[Saving Progress](./saving-progress.md). First please read through that guide
to understand the concept of "remote" playback and why coupling playback events
to the UI is a bad idea.

Once you've understood that concept, this concept is nearly identical. You would
leverage the same `Event.PlaybackProgressUpdated` event in this scenario too.

Here's how you would use an event to implement a sleep timer:

1. The user configures a sleep timer in the UI.
2. Persist the time they configure in a store as a timestamp.
3. Each time the progress event fires you check your persisted sleep timer timestamp.
    - IF `sleeptime !== null && sleeptime <= now` THEN pause.
