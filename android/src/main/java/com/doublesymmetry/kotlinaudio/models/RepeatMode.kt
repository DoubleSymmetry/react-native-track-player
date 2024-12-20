package com.doublesymmetry.kotlinaudio.models

enum class RepeatMode{
    OFF, ONE, ALL;

    companion object {
        fun fromOrdinal(ordinal: Int): RepeatMode {
            return when (ordinal) {
                0 -> OFF
                1 -> ONE
                2 -> ALL
                else -> error("Wrong ordinal")
            }
        }
    }
}