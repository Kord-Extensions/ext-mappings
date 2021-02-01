package com.kotlindiscord.kordex.ext.mappings

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.extensions.KoinExtension
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kordex.ext.mappings.arguments.*
import com.kotlindiscord.kordex.ext.mappings.configuration.MappingsConfigAdapter
import com.kotlindiscord.kordex.ext.mappings.enums.Channels
import com.kotlindiscord.kordex.ext.mappings.enums.YarnChannels
import com.kotlindiscord.kordex.ext.mappings.exceptions.UnsupportedNamespaceException
import com.kotlindiscord.kordex.ext.mappings.utils.classesToPages
import com.kotlindiscord.kordex.ext.mappings.utils.fieldsToPages
import com.kotlindiscord.kordex.ext.mappings.utils.methodsToPages
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.event.message.MessageCreateEvent
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.*
import me.shedaniel.linkie.utils.MappingsQuery
import me.shedaniel.linkie.utils.QueryContext
import mu.KotlinLogging
import org.koin.core.component.inject

private const val VERSION_CHUNK_SIZE = 10
private const val PAGE_FOOTER = "Powered by Linkie"
private const val PAGE_FOOTER_ICON =
    "https://cdn.discordapp.com/attachments/789139884307775580/790887070334976020/linkie_arrow.png"
private const val TIMEOUT_MULTIPLIER = 1000L  // To transform it into seconds

/**
 * Extension providing Minecraft mappings lookups on Discord.
 */
class MappingsExtension(bot: ExtensibleBot) : KoinExtension(bot) {
    private val config: MappingsConfigAdapter by inject()

    companion object {
        private var checks: MutableList<suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)> =
            mutableListOf()

        private var namespaceChecks: MutableList<suspend (Namespace) -> (suspend (MessageCreateEvent) -> Boolean)> =
            mutableListOf()

        /**
         * Internal function used to add a check to this extension.
         */
        fun addCheck(check: suspend (String) -> (suspend (MessageCreateEvent) -> Boolean)) =
            checks.add(check)

        /**
         * Internal function used to add a check to this extension.
         */
        fun addNamespaceCheck(check: suspend (Namespace) -> (suspend (MessageCreateEvent) -> Boolean)) =
            namespaceChecks.add(check)
    }

    private val logger = KotlinLogging.logger { }
    override val name: String = "mappings"

    override suspend fun setup() {
        val namespaces = mutableListOf<Namespace>()
        val enabledNamespaces = config.getEnabledNamespaces()

        enabledNamespaces.forEach {
            when (it) {
                "legacy-yarn" -> namespaces.add(LegacyYarnNamespace)
                "mcp" -> namespaces.add(MCPNamespace)
                "mojang" -> namespaces.add(MojangNamespace)
                "plasma" -> namespaces.add(PlasmaNamespace)
                "yarn" -> namespaces.add(YarnNamespace)
                "yarrn" -> namespaces.add(YarrnNamespace)

                else -> throw UnsupportedNamespaceException(it)
            }
        }

        if (namespaces.isEmpty()) {
            logger.warn { "No namespaces have been enabled, not registering commands." }
            return
        }

        Namespaces.init(LinkieConfig.DEFAULT.copy(namespaces = namespaces))

        val legacyYarnEnabled = enabledNamespaces.contains("legacy-yarn")
        val mcpEnabled = enabledNamespaces.contains("mcp")
        val mojangEnabled = enabledNamespaces.contains("mojang")
        val plasmaEnabled = enabledNamespaces.contains("plasma")
        val yarnEnabled = enabledNamespaces.contains("yarn")
        val yarrnEnabled = enabledNamespaces.contains("yarrn")

        val patchworkEnabled = config.yarnChannelEnabled(YarnChannels.PATCHWORK)

        val categoryCheck = allowedCategory(config.getAllowedCategories(), config.getBannedCategories())
        val channelCheck = allowedGuild(config.getAllowedChannels(), config.getBannedChannels())
        val guildCheck = allowedGuild(config.getAllowedGuilds(), config.getBannedGuilds())

        val yarnChannels = YarnChannels.values().filter {
            it != YarnChannels.PATCHWORK || patchworkEnabled
        }.joinToString(", ") { "`${it.str}`" }

        // region: Legacy Yarn mappings lookups

        if (legacyYarnEnabled) {
            // Class
            command {
                name = "lyc"
                aliases = arrayOf("lyarnc", "legacy-yarnc", "legacyyarnc", "legacyarnc")

                description = "Look up Legacy Yarn mappings info for a class.\n\n" +

                        "For more information or a list of versions for Legacy Yarn mappings, you can use the " +
                        "`lyarn` command."

                check(customChecks(name, LegacyYarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::LegacyYarnArguments)

                action {
                    val args: LegacyYarnArguments

                    message.channel.withTyping {
                        args = parse(::LegacyYarnArguments)
                    }

                    queryClasses(LegacyYarnNamespace, args.query, args.version)
                }
            }

            // Field
            command {
                name = "lyf"
                aliases = arrayOf("lyarnf", "legacy-yarnf", "legacyyarnf", "legacyarnf")

                description = "Look up Legacy Yarn mappings info for a field.\n\n" +

                        "For more information or a list of versions for Legacy Yarn mappings, you can use the " +
                        "`lyarn` command."

                check(customChecks(name, LegacyYarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::LegacyYarnArguments)

                action {
                    val args: LegacyYarnArguments

                    message.channel.withTyping {
                        args = parse(::LegacyYarnArguments)
                    }

                    queryFields(LegacyYarnNamespace, args.query, args.version)
                }
            }

            // Method
            command {
                name = "lym"
                aliases = arrayOf("lyarnm", "legacy-yarnm", "legacyyarnm", "legacyarnm")

                description = "Look up Legacy Yarn mappings info for a method.\n\n" +

                        "For more information or a list of versions for Legacy Yarn mappings, you can use the " +
                        "`lyarn` command."

                check(customChecks(name, LegacyYarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::LegacyYarnArguments)

                action {
                    val args: LegacyYarnArguments

                    message.channel.withTyping {
                        args = parse(::LegacyYarnArguments)
                    }

                    queryMethods(LegacyYarnNamespace, args.query, args.version)
                }
            }
        }

        // endregion

        // region: MCP mappings lookups

        if (mcpEnabled) {
            // Class
            command {
                name = "mcpc"

                description = "Look up MCP mappings info for a class.\n\n" +

                        "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check(customChecks(name, MCPNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MCPArguments)

                action {
                    val args: MCPArguments

                    message.channel.withTyping {
                        args = parse(::MCPArguments)
                    }

                    queryClasses(MCPNamespace, args.query, args.version)
                }
            }

            // Field
            command {
                name = "mcpf"

                description = "Look up MCP mappings info for a field.\n\n" +

                        "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check(customChecks(name, MCPNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MCPArguments)

                action {
                    val args: MCPArguments

                    message.channel.withTyping {
                        args = parse(::MCPArguments)
                    }

                    queryFields(MCPNamespace, args.query, args.version)
                }
            }

            // Method
            command {
                name = "mcpm"

                description = "Look up MCP mappings info for a method.\n\n" +

                        "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check(customChecks(name, MCPNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MCPArguments)

                action {
                    val args: MCPArguments

                    message.channel.withTyping {
                        args = parse(::MCPArguments)
                    }

                    queryMethods(MCPNamespace, args.query, args.version)
                }
            }
        }

        // endregion

        // region: Mojang mappings lookups

        if (mojangEnabled) {
            // Class
            command {
                name = "mmc"
                aliases = arrayOf("mojc", "mojmapc")

                description = "Look up Mojang mappings info for a class.\n\n" +

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                check(customChecks(name, MojangNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryClasses(MojangNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Field
            command {
                name = "mmf"
                aliases = arrayOf("mojf", "mojmapf")

                description = "Look up Mojang mappings info for a field.\n\n" +

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                check(customChecks(name, MojangNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryFields(MojangNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Method
            command {
                name = "mmm"
                aliases = arrayOf("mojm", "mojmapm")

                description = "Look up Mojang mappings info for a method.\n\n" +

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                        "\n\n" +

                        "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                        "command."

                check(customChecks(name, MojangNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::MojangArguments)

                action {
                    val args: MojangArguments

                    message.channel.withTyping {
                        args = parse(::MojangArguments)
                    }

                    queryMethods(MojangNamespace, args.query, args.version, args.channel?.str)
                }
            }
        }

        // endregion

        // region: Plasma mappings lookups

        if (plasmaEnabled) {
            // Class
            command {
                name = "pc"

                description = "Look up Plasma mappings info for a class.\n\n" +

                        "For more information or a list of versions for Plasma mappings, you can use the " +
                        "`plasma` command."

                check(customChecks(name, PlasmaNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::PlasmaArguments)

                action {
                    val args: PlasmaArguments

                    message.channel.withTyping {
                        args = parse(::PlasmaArguments)
                    }

                    queryClasses(PlasmaNamespace, args.query, args.version)
                }
            }

            // Field
            command {
                name = "pf"

                description = "Look up Plasma mappings info for a field.\n\n" +

                        "For more information or a list of versions for Plasma mappings, you can use the " +
                        "`plasma` command."

                check(customChecks(name, PlasmaNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::PlasmaArguments)

                action {
                    val args: PlasmaArguments

                    message.channel.withTyping {
                        args = parse(::PlasmaArguments)
                    }

                    queryFields(PlasmaNamespace, args.query, args.version)
                }
            }

            // Method
            command {
                name = "pm"

                description = "Look up Plasma mappings info for a method.\n\n" +

                        "For more information or a list of versions for Plasma mappings, you can use the " +
                        "`plasma` command."

                check(customChecks(name, PlasmaNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::PlasmaArguments)

                action {
                    val args: PlasmaArguments

                    message.channel.withTyping {
                        args = parse(::PlasmaArguments)
                    }

                    queryMethods(PlasmaNamespace, args.query, args.version)
                }
            }
        }

        // endregion

        // region: Yarn mappings lookups

        if (yarnEnabled) {
            // Class
            command {
                name = "yc"
                aliases = arrayOf("yarnc")

                description = "Look up Yarn mappings info for a class.\n\n" +

                        "**Channels:** $yarnChannels" +
                        "\n\n" +

                        "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                        "command."

                check(customChecks(name, YarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature { YarnArguments(patchworkEnabled) }

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse { YarnArguments(patchworkEnabled) }
                    }

                    if (!patchworkEnabled && args.channel == YarnChannels.PATCHWORK) {
                        message.respond("Patchwork support is currently disabled.")
                        return@action
                    }

                    queryClasses(YarnNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Field
            command {
                name = "yf"
                aliases = arrayOf("yarnf")

                description = "Look up Yarn mappings info for a field.\n\n" +

                        "**Channels:** $yarnChannels" +
                        "\n\n" +

                        "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                        "command."

                check(customChecks(name, YarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature { YarnArguments(patchworkEnabled) }

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse { YarnArguments(patchworkEnabled) }
                    }

                    if (!patchworkEnabled && args.channel == YarnChannels.PATCHWORK) {
                        message.respond("Patchwork support is currently disabled.")
                        return@action
                    }

                    queryFields(YarnNamespace, args.query, args.version, args.channel?.str)
                }
            }

            // Method
            command {
                name = "ym"
                aliases = arrayOf("yarnm")

                description = "Look up Yarn mappings info for a method.\n\n" +

                        "**Channels:** $yarnChannels" +
                        "\n\n" +

                        "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                        "command."

                check(customChecks(name, YarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature { YarnArguments(patchworkEnabled) }

                action {
                    val args: YarnArguments

                    message.channel.withTyping {
                        args = parse { YarnArguments(patchworkEnabled) }
                    }

                    if (!patchworkEnabled && args.channel == YarnChannels.PATCHWORK) {
                        message.respond("Patchwork support is currently disabled.")
                        return@action
                    }

                    queryMethods(YarnNamespace, args.query, args.version, args.channel?.str)
                }
            }
        }

        // endregion

        // region: Yarrn mappings lookups

        if (yarrnEnabled) {
            // Class
            command {
                name = "yrc"

                description = "Look up Yarrn mappings info for a class.\n\n" +

                        "For more information or a list of versions for Yarrn mappings, you can use the " +
                        "`yarrn` command."

                check(customChecks(name, YarrnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::YarrnArguments)

                action {
                    val args: YarrnArguments

                    message.channel.withTyping {
                        args = parse(::YarrnArguments)
                    }

                    queryClasses(YarrnNamespace, args.query, args.version)
                }
            }

            // Field
            command {
                name = "yrf"

                description = "Look up Yarrn mappings info for a field.\n\n" +

                        "For more information or a list of versions for Yarrn mappings, you can use the " +
                        "`yarrn` command."

                check(customChecks(name, YarrnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::YarrnArguments)

                action {
                    val args: YarrnArguments

                    message.channel.withTyping {
                        args = parse(::YarrnArguments)
                    }

                    queryFields(YarrnNamespace, args.query, args.version)
                }
            }

            // Method
            command {
                name = "yrm"

                description = "Look up Yarrn mappings info for a method.\n\n" +

                        "For more information or a list of versions for Yarrn mappings, you can use the " +
                        "`yarrn` command."

                check(customChecks(name, YarrnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks
                signature(::YarrnArguments)

                action {
                    val args: YarrnArguments

                    message.channel.withTyping {
                        args = parse(::YarrnArguments)
                    }

                    queryMethods(YarrnNamespace, args.query, args.version)
                }
            }
        }

        // endregion

        // region: Mappings info commands

        if (legacyYarnEnabled) {
            command {
                name = "lyarn"
                aliases = arrayOf("legacy-yarn", "legacyyarn", "legacyarn")

                description = "Get information and a list of supported versions for Legacy Yarn mappings."

                check(customChecks(name, LegacyYarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = LegacyYarnNamespace.getDefaultVersion()
                    val allVersions = LegacyYarnNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            if (version == defaultVersion) {
                                "**» $version** (Default)"
                            } else {
                                "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "Legacy Yarn mappings are available for queries across **${allVersions.size}** " +
                                "versions.\n\n" +

                                "**Default version:** $defaultVersion\n" +
                                "**Commands:** `lyc`, `lyf`, `lym`\n\n" +

                                "For a full list of supported Yarn versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Legacy Yarn"

                    pages.forEach {
                        pagesObj.addPage(
                            Page(
                                description = it,
                                title = pageTitle,
                                footer = PAGE_FOOTER,
                                footerIcon = PAGE_FOOTER_ICON
                            )
                        )
                    }

                    val paginator = Paginator(
                        bot,
                        targetMessage = message,
                        pages = pagesObj,
                        owner = message.author,
                        keepEmbed = true,
                        timeout = getTimeout()
                    )

                    paginator.send()
                }
            }
        }

        if (mcpEnabled) {
            command {
                name = "mcp"

                description = "Get information and a list of supported versions for MCP mappings."

                check(customChecks(name, MCPNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = MCPNamespace.getDefaultVersion()
                    val allVersions = MCPNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            if (version == defaultVersion) {
                                "**» $version** (Default)"
                            } else {
                                "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "MCP mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                                "**Default version:** $defaultVersion\n" +
                                "**Commands:** `mcpc`, `mcpf`, `mcpm`\n\n" +

                                "For a full list of supported MCP versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: MCP"

                    pages.forEach {
                        pagesObj.addPage(
                            Page(
                                description = it,
                                title = pageTitle,
                                footer = PAGE_FOOTER,
                                footerIcon = PAGE_FOOTER_ICON
                            )
                        )
                    }

                    val paginator = Paginator(
                        bot,
                        targetMessage = message,
                        pages = pagesObj,
                        owner = message.author,
                        keepEmbed = true,
                        timeout = getTimeout()
                    )

                    paginator.send()
                }
            }
        }

        if (mojangEnabled) {
            command {
                name = "mojang"
                aliases = arrayOf("mojmap")

                description = "Get information and a list of supported versions for Mojang mappings."

                check(customChecks(name, MojangNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = MojangNamespace.getDefaultVersion()
                    val allVersions = MojangNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            if (version == defaultVersion) {
                                "**» $version** (Default)"
                            } else {
                                "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "Mojang mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                                "**Default version:** $defaultVersion\n\n" +

                                "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                                "\n" +
                                "**Commands:** `mmc`, `mmf`, `mmm`\n\n" +

                                "For a full list of supported Mojang versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Mojang"

                    pages.forEach {
                        pagesObj.addPage(
                            Page(
                                description = it,
                                title = pageTitle,
                                footer = PAGE_FOOTER,
                                footerIcon = PAGE_FOOTER_ICON
                            )
                        )
                    }

                    val paginator = Paginator(
                        bot,
                        targetMessage = message,
                        pages = pagesObj,
                        owner = message.author,
                        keepEmbed = true,
                        timeout = getTimeout()
                    )

                    paginator.send()
                }
            }
        }

        if (plasmaEnabled) {
            command {
                name = "plasma"

                description = "Get information and a list of supported versions for Plasma mappings."

                check(customChecks(name, PlasmaNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = PlasmaNamespace.getDefaultVersion()
                    val allVersions = PlasmaNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            if (version == defaultVersion) {
                                "**» $version** (Default)"
                            } else {
                                "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "Plasma mappings are available for queries across **${allVersions.size}** " +
                                "versions.\n\n" +

                                "**Default version:** $defaultVersion\n" +
                                "**Commands:** `pc`, `pf`, `pm`\n\n" +

                                "For a full list of supported Plasma versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Plasma"

                    pages.forEach {
                        pagesObj.addPage(
                            Page(
                                description = it,
                                title = pageTitle,
                                footer = PAGE_FOOTER,
                                footerIcon = PAGE_FOOTER_ICON
                            )
                        )
                    }

                    val paginator = Paginator(
                        bot,
                        targetMessage = message,
                        pages = pagesObj,
                        owner = message.author,
                        keepEmbed = true,
                        timeout = getTimeout()
                    )

                    paginator.send()
                }
            }
        }

        if (yarnEnabled) {
            command {
                name = "yarn"

                description = "Get information and a list of supported versions for Yarn mappings."

                check(customChecks(name, YarnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultPatchworkVersion = if (patchworkEnabled) {
                        YarnNamespace.getDefaultVersion { YarnChannels.PATCHWORK.str }
                    } else {
                        ""
                    }

                    val defaultVersion = YarnNamespace.getDefaultVersion()
                    val defaultSnapshotVersion = YarnNamespace.getDefaultVersion { YarnChannels.SNAPSHOT.str }
                    val allVersions = YarnNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            when (version) {
                                defaultVersion -> "**» $version** (Default)"
                                defaultSnapshotVersion -> "**» $version** (Default: Snapshot)"

                                defaultPatchworkVersion -> if (patchworkEnabled) {
                                    "**» $version** (Default: Patchwork)"
                                } else {
                                    "**»** $version"
                                }

                                else -> "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "Yarn mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                                "**Default version:** $defaultVersion\n" +
                                "**Default snapshot version:** $defaultSnapshotVersion\n\n" +

                                if (patchworkEnabled) {
                                    "**Default Patchwork version:** $defaultPatchworkVersion\n\n"
                                } else {
                                    ""
                                } +

                                "**Channels:** $yarnChannels\n" +
                                "**Commands:** `yc`, `yf`, `ym`\n\n" +

                                "For a full list of supported Yarn versions, please view the rest of the pages." +

                                if (legacyYarnEnabled) {
                                    " For Legacy Yarn mappings, please see the `lyarn` command."
                                } else {
                                    ""
                                }
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Yarn"

                    pages.forEach {
                        pagesObj.addPage(
                            Page(
                                description = it,
                                title = pageTitle,
                                footer = PAGE_FOOTER,
                                footerIcon = PAGE_FOOTER_ICON
                            )
                        )
                    }

                    val paginator = Paginator(
                        bot,
                        targetMessage = message,
                        pages = pagesObj,
                        owner = message.author,
                        keepEmbed = true,
                        timeout = getTimeout()
                    )

                    paginator.send()
                }
            }
        }

        if (yarrnEnabled) {
            command {
                name = "yarrn"

                description = "Get information and a list of supported versions for Yarrn mappings."

                check(customChecks(name, YarrnNamespace))
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = YarrnNamespace.getDefaultVersion()
                    val allVersions = YarrnNamespace.getAllSortedVersions()

                    val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                        it.joinToString("\n") { version ->
                            if (version == defaultVersion) {
                                "**» $version** (Default)"
                            } else {
                                "**»** $version"
                            }
                        }
                    }.toMutableList()

                    pages.add(
                        0,
                        "Yarrn mappings are available for queries across **${allVersions.size}** " +
                                "versions.\n\n" +

                                "**Default version:** $defaultVersion\n" +
                                "**Commands:** `yrc`, `yrf`, `yrm`\n\n" +

                                "For a full list of supported Yarrn versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Yarrn"

                    pages.forEach {
                        pagesObj.addPage(
                            Page(
                                description = it,
                                title = pageTitle,
                                footer = PAGE_FOOTER,
                                footerIcon = PAGE_FOOTER_ICON
                            )
                        )
                    }

                    val paginator = Paginator(
                        bot,
                        targetMessage = message,
                        pages = pagesObj,
                        owner = message.author,
                        keepEmbed = true,
                        timeout = getTimeout()
                    )

                    paginator.send()
                }
            }
        }

        // endregion

        logger.info { "Mappings extension set up - namespaces: " + enabledNamespaces.joinToString(", ") }
    }

    private suspend fun CommandContext.queryClasses(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        val provider = if (version == null) {
            if (channel != null) {
                namespace.getProvider(
                    namespace.getDefaultVersion { channel }
                )
            } else {
                MappingsProvider.empty(namespace)
            }
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                channel ?: namespace.getDefaultMappingChannel()
            }
        )

        val query = givenQuery.replace(".", "/")
        var pages: List<Pair<String, String>>

        message.channel.withTyping {
            @Suppress("TooGenericExceptionCaught")
            val result = try {
                MappingsQuery.queryClasses(
                    QueryContext(
                        provider = provider,
                        searchKey = query
                    )
                )
            } catch (e: NullPointerException) {
                message.respond(e.localizedMessage)
                return@queryClasses
            }

            pages = classesToPages(namespace, result)
        }

        if (pages.isEmpty()) {
            message.respond("No results found")
            return
        }

        val meta = provider.get()

        val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")
        val pageTitle = "List of ${meta.name} classes: ${meta.version}"

        val shortPages = mutableListOf<String>()
        val longPages = mutableListOf<String>()

        pages.forEach { (short, long) ->
            shortPages.add(short)
            longPages.add(long)
        }

        shortPages.forEach {
            pagesObj.addPage(
                "${EXPAND_EMOJI.mention} for more",

                Page(
                    description = it,
                    title = pageTitle,
                    footer = PAGE_FOOTER,
                    footerIcon = PAGE_FOOTER_ICON
                )
            )
        }

        if (shortPages != longPages) {
            longPages.forEach {
                pagesObj.addPage(
                    "${EXPAND_EMOJI.mention} for less",

                    Page(
                        description = it,
                        title = pageTitle,
                        footer = PAGE_FOOTER,
                        footerIcon = PAGE_FOOTER_ICON
                    )
                )
            }
        }

        val paginator = Paginator(
            bot,
            targetMessage = message,
            pages = pagesObj,
            owner = message.author,
            keepEmbed = true,
            timeout = getTimeout()
        )

        paginator.send()
    }

    private suspend fun CommandContext.queryFields(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        val provider = if (version == null) {
            if (channel != null) {
                namespace.getProvider(
                    namespace.getDefaultVersion { channel }
                )
            } else {
                MappingsProvider.empty(namespace)
            }
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                channel ?: namespace.getDefaultMappingChannel()
            }
        )

        val query = givenQuery.replace(".", "/")
        var pages: List<Pair<String, String>>

        message.channel.withTyping {
            @Suppress("TooGenericExceptionCaught")
            val result = try {
                MappingsQuery.queryFields(
                    QueryContext(
                        provider = provider,
                        searchKey = query
                    )
                )
            } catch (e: NullPointerException) {
                message.respond(e.localizedMessage)
                return@queryFields
            }

            pages = fieldsToPages(namespace, provider.get(), result)
        }

        if (pages.isEmpty()) {
            message.respond("No results found")
            return
        }

        val meta = provider.get()

        val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")
        val pageTitle = "List of ${meta.name} fields: ${meta.version}"

        val shortPages = mutableListOf<String>()
        val longPages = mutableListOf<String>()

        pages.forEach { (short, long) ->
            shortPages.add(short)
            longPages.add(long)
        }

        shortPages.forEach {
            pagesObj.addPage(
                "${EXPAND_EMOJI.mention} for more",

                Page(
                    description = it,
                    title = pageTitle,
                    footer = PAGE_FOOTER,
                    footerIcon = PAGE_FOOTER_ICON
                )
            )
        }

        if (shortPages != longPages) {
            longPages.forEach {
                pagesObj.addPage(
                    "${EXPAND_EMOJI.mention} for less",

                    Page(
                        description = it,
                        title = pageTitle,
                        footer = PAGE_FOOTER,
                        footerIcon = PAGE_FOOTER_ICON
                    )
                )
            }
        }

        val paginator = Paginator(
            bot,
            targetMessage = message,
            pages = pagesObj,
            owner = message.author,
            keepEmbed = true,
            timeout = getTimeout()
        )

        paginator.send()
    }

    private suspend fun CommandContext.queryMethods(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        val provider = if (version == null) {
            if (channel != null) {
                namespace.getProvider(
                    namespace.getDefaultVersion { channel }
                )
            } else {
                MappingsProvider.empty(namespace)
            }
        } else {
            namespace.getProvider(version.version)
        }

        provider.injectDefaultVersion(
            namespace.getDefaultProvider {
                channel ?: namespace.getDefaultMappingChannel()
            }
        )

        val query = givenQuery.replace(".", "/")
        var pages: List<Pair<String, String>>

        message.channel.withTyping {
            @Suppress("TooGenericExceptionCaught")
            val result = try {
                MappingsQuery.queryMethods(
                    QueryContext(
                        provider = provider,
                        searchKey = query
                    )
                )
            } catch (e: NullPointerException) {
                message.respond(e.localizedMessage)
                return@queryMethods
            }

            pages = methodsToPages(namespace, provider.get(), result)
        }

        if (pages.isEmpty()) {
            message.respond("No results found")
            return
        }

        val meta = provider.get()

        val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")
        val pageTitle = "List of ${meta.name} methods: ${meta.version}"

        val shortPages = mutableListOf<String>()
        val longPages = mutableListOf<String>()

        pages.forEach { (short, long) ->
            shortPages.add(short)
            longPages.add(long)
        }

        shortPages.forEach {
            pagesObj.addPage(
                "${EXPAND_EMOJI.mention} for more",

                Page(
                    description = it,
                    title = pageTitle,
                    footer = PAGE_FOOTER,
                    footerIcon = PAGE_FOOTER_ICON
                )
            )
        }

        if (shortPages != longPages) {
            longPages.forEach {
                pagesObj.addPage(
                    "${EXPAND_EMOJI.mention} for less",

                    Page(
                        description = it,
                        title = pageTitle,
                        footer = PAGE_FOOTER,
                        footerIcon = PAGE_FOOTER_ICON
                    )
                )
            }
        }

        val paginator = Paginator(
            bot,
            targetMessage = message,
            pages = pagesObj,
            owner = message.author,
            keepEmbed = true,
            timeout = getTimeout()
        )

        paginator.send()
    }

    private suspend fun getTimeout() = config.getTimeout() * TIMEOUT_MULTIPLIER

    private suspend fun customChecks(command: String, namespace: Namespace): suspend (MessageCreateEvent) -> Boolean {
        val allChecks = checks.map { it.invoke(command) }
        val allNamespaceChecks = namespaceChecks.map { it.invoke(namespace) }

        suspend fun inner(event: MessageCreateEvent): Boolean =
            allChecks.all { it.invoke(event) }.and(
                allNamespaceChecks.all { it.invoke(event) }
            )

        return ::inner
    }
}
