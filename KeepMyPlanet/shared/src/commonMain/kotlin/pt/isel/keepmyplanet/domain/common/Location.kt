package pt.isel.keepmyplanet.domain.common

data class Location(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90.0 and 90.0 (was $latitude)."
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180.0 and 180.0 (was $longitude)."
        }
    }
}
