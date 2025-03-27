package pt.isel.keepmyplanet.domain.src

import kotlin.jvm.JvmInline

@JvmInline
value class Id(
    private val id: UInt,
) {
    init {
        require(id > 0u) { "Id must be greater than 0" }
    }
}
