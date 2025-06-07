package pt.isel.keepmyplanet.util

inline fun <reified T : Enum<T>> safeValueOf(value: String?): T? =
    value?.let {
        enumValues<T>().find { enum -> enum.name.equals(it, ignoreCase = true) }
    }
