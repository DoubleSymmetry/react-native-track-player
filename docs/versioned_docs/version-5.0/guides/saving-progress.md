---
sidebar_position: 2
---

# Saving Progress

A common use-case is to store the users progress on a particular `Track`
somewhere so that when they leave and come back, they can pick up right where
they left off. To do this you need to listen for progress updates and then
store the progress somewhere. There are two high level ways of getting this
done.

## Naive Approach

One approach could be to use the progress events/updates that the `useProgress`
hook provides. This isn't a very good idea and here's why:

Users can listen to audio both "in-App" and "Remotely". In-App would be defined
as playback while the user has the app opened on screen. However, whenever
audio is being played in the background/remotely. For example: playback on the
lockscreen, carplay, etc. In these situations **the UI is not mounted**, meaning
the `useProgress` hook, or really any event listeners that are registered
inside of your App UI tree (anything called as a result of
`AppRegistry.registerComponent(appName, () => App);` in your `index.js` file)
**WILL NOT EXECUTE**.

In a nutshell, if you do this, your progress **will not** update when the user
is playing back in Remote contexts and therefore your app will seem buggy.

## Recommended Approach

The correct way to handle this is to track progress in the
[Playback Service](../basics/playback-service.md), based on the
`Event.PlaybackProgressUpdated` event. These events fire all the time, including
when your app is playing back remotely.
