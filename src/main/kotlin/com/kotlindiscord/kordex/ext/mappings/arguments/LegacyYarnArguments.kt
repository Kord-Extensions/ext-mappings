package com.kotlindiscord.kordex.ext.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kordex.ext.mappings.converters.optionalMappingsVersion
import me.shedaniel.linkie.namespaces.LegacyYarnNamespace

/** Arguments for Legacy Yarn mappings lookup commands. **/
@Suppress("UndocumentedPublicProperty")
class LegacyYarnArguments : Arguments() {
    val query by string("query")
    val version by optionalMappingsVersion("version", true, LegacyYarnNamespace)
}
