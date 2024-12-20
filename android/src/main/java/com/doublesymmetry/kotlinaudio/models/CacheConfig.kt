package com.doublesymmetry.kotlinaudio.models

/**
 * Configuration for cache properties of player.
 */
data class CacheConfig(
    /**
     * Maximum player cache size in kilobytes.
     */
    val maxCacheSize: Long?,

    /**
     * Cache identifier, used to make cache directory.
     */
    val identifier: String = "TrackPlayer"
)

