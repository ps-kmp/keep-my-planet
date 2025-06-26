package pt.isel.keepmyplanet.repository

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Photo

interface PhotoRepository : Repository<Photo, Id>
