package pt.isel.keepmyplanet.domain.zone

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(state.isNotBlank()) { "State cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
    }
}
