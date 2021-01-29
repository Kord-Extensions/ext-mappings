package com.kotlindiscord.kordex.ext.mappings.configuration.spec

import com.kotlindiscord.kordex.ext.mappings.enums.YarnChannels
import com.uchuhimo.konf.ConfigSpec

/** @suppress **/
object YarnSpec : ConfigSpec() {
    val channels by required<List<YarnChannels>>()
}
