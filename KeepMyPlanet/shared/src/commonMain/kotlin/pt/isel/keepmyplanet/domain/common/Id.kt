package pt.isel.keepmyplanet.domain.common

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Id(
    val value: UInt,
)
