package com.kotlindiscord.kordex.ext.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.optionalEnum
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.ext.mappings.converters.optionalMappingsVersion
import com.kotlindiscord.kordex.ext.mappings.enums.YarnChannels
import me.shedaniel.linkie.namespaces.YarnNamespace

/** Arguments for Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarnArguments(patchworkEnabled: Boolean) : Arguments() {
    val query by string("query")

    val channel by optionalEnum<YarnChannels>(
        "channel",

        "official/snapshot" + if (patchworkEnabled) {
            "/patchwork"
        } else {
            ""
        }
    )

    val version by optionalMappingsVersion("version", true, YarnNamespace)
}
