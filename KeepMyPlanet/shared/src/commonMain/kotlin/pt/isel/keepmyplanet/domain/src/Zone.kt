package pt.isel.keepmyplanet.domain.src

data class Zone(
    //val id: Long,
    val address: Address,
    val description: Description,
    val area: Location,
    val reported: Date,
    val photos: List<Photo>,
    val critical: Boolean,
    //val status: ZoneStatus,
    //val reporter: User,
    //val events: MutableList<Event> = mutableListOf()
)
