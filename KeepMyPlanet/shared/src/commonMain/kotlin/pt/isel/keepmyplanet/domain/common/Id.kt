package pt.isel.keepmyplanet.domain.common

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Id(
    val value: UInt,
)
