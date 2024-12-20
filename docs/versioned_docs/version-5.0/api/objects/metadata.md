# AudioMetadataReceivedEvent

An object representing the timed or chapter metadata received for a track.

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| metadata    | `AudioMetadata[]` | The metadata received                      |

# AudioCommonMetadataReceivedEvent

An object representing the common metadata received for a track.

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| metadata    | `AudioCommonMetadata` | The metadata received                      |

# AudioCommonMetadata

An object representing the common metadata received for a track.

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| title    | `string` | The track title. Might be undefined                      |
| artist   | `string` | The track artist. Might be undefined                     |
| albumTitle | `string` | The track album. Might be undefined                      |
| subtitle | `string` | The track subtitle. Might be undefined                   |
| description | `string` | The track description. Might be undefined              |
| artworkUri | `string` | The track artwork uri. Might be undefined              |
| trackNumber | `string` | The track number. Might be undefined              |
| composer | `string` | The track composer. Might be undefined              |
| conductor | `string` | The track conductor. Might be undefined              |
| genre | `string` | The track genre. Might be undefined              |
| compilation | `string` | The track compilation. Might be undefined              |
| station | `string` | The track station. Might be undefined              |
| mediaType | `string` | The track media type. Might be undefined              |
| creationDate | `string` | The track creation date. Might be undefined              |
| creationYear | `string` | The track creation year. Might be undefined              |

# AudioMetadata

An extension of `AudioCommonMetadataReceivedEvent` that includes the raw metadata.

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| raw | `RawEntry[]` | The raw metadata that was used to populate. May contain other non common keys. May be empty              |

# RawEntry

An object representing a raw metadata entry.

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| commonKey    | `string` | The common key. Might be undefined                      |
| keySpace   | `string` | The key space. Might be undefined                     |
| time | `number` | The time. Might be undefined                      |
| value | `unknown` | The value. Might be undefined                   |
| key | `string` | The key. Might be undefined                   |
