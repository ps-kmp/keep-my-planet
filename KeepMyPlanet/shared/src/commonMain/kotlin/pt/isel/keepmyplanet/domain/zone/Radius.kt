package pt.isel.keepmyplanet.domain.zone

import kotlin.jvm.JvmInline

@JvmInline
value class Radius(
    val value: Double,
) {
    init {
        require(value > 0) { "Radius must be a positive value." }
        require(value <= 5000) { "Radius cannot exceed 5000 meters." }
    }
}
