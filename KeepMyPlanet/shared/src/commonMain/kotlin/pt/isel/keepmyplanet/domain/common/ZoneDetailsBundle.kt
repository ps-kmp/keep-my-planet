package pt.isel.keepmyplanet.domain.common

import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.zone.Zone

data class ZoneDetailsBundle(
    val zone: Zone,
    val reporter: UserInfo,
)
