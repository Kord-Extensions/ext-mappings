package com.kotlindiscord.kordex.ext.mappings.enums

/**
 * Enum representing available Yarn channels.
 *
 * @property str String name used for the channel by Linkie
 */
enum class Channels(val str: String) {
    OFFICIAL("official"),
    SNAPSHOT("snapshot")
}
