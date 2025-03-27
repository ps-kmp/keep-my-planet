package pt.isel.keepmyplanet.domain.src

data class Photo(
    val url: String,
    val description: String,
) {
    init {
        require(url.isNotBlank()) { "URL cannot be blank" }
        require(description.isNotBlank()) { "Description cannot be blank" }
    }
}
