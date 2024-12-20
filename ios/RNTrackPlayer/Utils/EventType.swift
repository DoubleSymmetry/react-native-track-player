import Foundation

enum EventType: String, CaseIterable {
    case RemoteDuck = "remote-duck"
    case RemoteSeek = "remote-seek"
    case RemoteNext = "remote-next"
    case RemotePrevious = "remote-previous"
    case RemoteStop = "remote-stop"
    case RemotePause = "remote-pause"
    case RemotePlay = "remote-play"
    case RemoteJumpForward = "remote-jump-forward"
    case RemoteJumpBackward = "remote-jump-backward"
    case RemoteLike = "remote-like"
    case RemoteDislike = "remote-dislike"
    case RemoteBookmark = "remote-bookmark"
    case PlaybackMetadataReceived = "playback-metadata-received"
    case PlaybackError = "playback-error"
    case PlaybackQueueEnded = "playback-queue-ended"
    case PlaybackActiveTrackChanged = "playback-active-track-changed"
    case PlaybackState = "playback-state"
    case PlaybackProgressUpdated = "playback-progress-updated"
    case PlaybackPlayWhenReadyChanged = "playback-play-when-ready-changed"
    case SleepTimerChanged = "sleep-timer-changed"
    case SleepTimerComplete = "sleep-timer-complete"
    case MetadataChapterReceived = "metadata-chapter-received"
    case MetadataTimedReceived = "metadata-timed-received"
    case MetadataCommonReceived = "metadata-common-received"

    static func allRawValues() -> [String] {
        return allCases.map { $0.rawValue }
    }
}
