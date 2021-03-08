@file:OptIn(ConverterToOptional::class)

package com.kotlindiscord.kordex.ext.mappings.converters

import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import me.shedaniel.linkie.Namespace

/** Mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.mappingsVersion(displayName: String, description: String, namespaceGetter: suspend () -> Namespace) =
    arg(displayName, description, MappingsVersionConverter(namespaceGetter))

/** Mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.mappingsVersion(displayName: String, description: String, namespace: Namespace) =
    arg(displayName, description, MappingsVersionConverter { namespace })

/** Mappings namespace converter; see KordEx bundled functions for more info. **/
fun Arguments.namespace(displayName: String, description: String) =
    arg(displayName, description, NamespaceConverter())

/** Optional mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalMappingsVersion(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    namespaceGetter: suspend () -> Namespace
) =
    arg(displayName, description, MappingsVersionConverter(namespaceGetter).toOptional(outputError = outputError))

/** Optional mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalMappingsVersion(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    namespace: Namespace
) =
    arg(displayName, description, MappingsVersionConverter { namespace }.toOptional(outputError = outputError))

/** Optional mappings namespace converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalNamespace(displayName: String, description: String, outputError: Boolean = false) =
    arg(displayName, description, NamespaceConverter().toOptional(outputError = outputError))
