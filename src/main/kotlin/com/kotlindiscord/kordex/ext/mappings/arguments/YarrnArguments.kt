package com.kotlindiscord.kordex.ext.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.ext.mappings.converters.optionalMappingsVersion
import me.shedaniel.linkie.namespaces.YarrnNamespace

/** Arguments for Mojang mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class YarrnArguments : Arguments() {
    val query by string("query", "Name to query mappings for")

    val version by optionalMappingsVersion(
        "version",
        "Minecraft version to use for this query",
        true,
        YarrnNamespace
    )
}
