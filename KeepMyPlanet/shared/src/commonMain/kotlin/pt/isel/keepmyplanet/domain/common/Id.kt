package pt.isel.keepmyplanet.domain.common

import kotlin.jvm.JvmInline

@JvmInline
value class Id(
    val value: UInt,
) {
    init {
        require(value > 0u) { "Id must be greater than 0" }
    }
}
