---
sidebar_position: 3
---

# Rating

All RatingType types are made available through the named export `RatingType`:

```ts
import { RatingType } from 'react-native-track-player';
```

| Name | Description |
|------|-------------|
| `Heart`        | Rating type indicating "with heart" or "without heart", its value is a `boolean`. |
| `ThumbsUpDown` | Rating type indicating "thumbs up" or "thumbs down", its value is a `boolean`. |
| `ThreeStars`   | Rating type indicating 0 to 3 stars, its value is a `number` of stars. |
| `FourStars`    | Rating type indicating 0 to 4 stars, its value is a `number` of stars. |
| `FiveStars`    | Rating type indicating 0 to 5 stars, its value is a `number` of stars. |
| `Percentage`   | Rating type indicating percentage, its value is a `number`. |
