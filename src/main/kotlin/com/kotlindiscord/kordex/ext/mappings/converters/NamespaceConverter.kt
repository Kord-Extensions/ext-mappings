package com.kotlindiscord.kordex.ext.mappings.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.Namespaces

/**
 * Argument converter for [Namespace] objects.
 */
class NamespaceConverter : SingleConverter<Namespace>() {
    override val signatureTypeString: String = "mappings"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = Namespaces.namespaces[arg] ?: throw ParseException("Invalid mappings namespace: `$arg`")

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
