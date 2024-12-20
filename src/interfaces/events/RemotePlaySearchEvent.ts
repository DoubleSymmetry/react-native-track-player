export interface RemotePlaySearchEvent {
  /*
  below is the Google Voice event log:
  Event.RemotePlaySearchEvent {
    "android.intent.extra.REFERRER_NAME": "android-app://com.google.android.googlequicksearchbox/https/www.google.com",
    "android.intent.extra.START_PLAYBACK": true,
    "android.intent.extra.focus": "vnd.android.cursor.item/*",
    "android.intent.extra.user_query": "play seat",
    "android.intent.extra.user_query_language": "en-US",
    "com.google.android.projection.gearhead.ignore_original_pkg": false,
    "opa_allow_launch_intent_on_lockscreen": true,
    "query": "seat"} 
  */
  query: string;
  focus?: 'artist' | 'album' | 'playlist' | 'genre';
  title?: string;
  artist?: string;
  album?: string;
  date?: string;
  playlist?: string;
}
