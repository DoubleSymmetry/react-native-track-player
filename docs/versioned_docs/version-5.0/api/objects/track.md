---
sidebar_position: 1
---

# Track

Tracks in the player queue are plain javascript objects as described below.

Only the `url`, `title` and `artist` properties are required for basic playback

| Param          | Type                        | Description  |
| -------------- | --------------------------- | ------------ |
| id             | `string`                    | The track id                |
| url            | `string` or [Resource Object](../objects/resource.md) | The media URL |
| type           | `string`                    | Stream type. One of `dash`, `hls`, `smoothstreaming` or `default` |
| userAgent      | `string`                    | The user agent HTTP header  |
| contentType    | `string`                    | Mime type of the media file |
| duration       | `number`                    | The duration in seconds     |
| title          | `string`                    | The track title             |
| artist         | `string`                    | The track artist            |
| album          | `string`                    | The track album             |
| description    | `string`                    | The track description       |
| genre          | `string`                    | The track genre             |
| date           | `string`                    | The track release date in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) |
| rating         | Depends on the [rating type](../constants/rating.md)  | The track rating value |
| artwork        | `string` or [Resource Object](../objects/resource.md) | The artwork url |
| pitchAlgorithm | [Pitch Algorithm](../constants/pitch-algorithm.md) | The pitch algorithm |
| headers        | `object`                    | An object containing all the headers to use in the HTTP request |
| isLiveStream   | `boolean`                   | Used by iOS to present live stream option in control center |
