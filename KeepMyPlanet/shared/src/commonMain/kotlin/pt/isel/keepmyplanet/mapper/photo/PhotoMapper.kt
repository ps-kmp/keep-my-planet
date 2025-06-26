package pt.isel.keepmyplanet.mapper.photo

import pt.isel.keepmyplanet.domain.common.Photo
import pt.isel.keepmyplanet.dto.photo.PhotoResponse

fun Photo.toResponse(): PhotoResponse =
    PhotoResponse(
        id = this.id.value,
        url = this.url.value,
        uploaderId = this.uploaderId.value,
        uploadedAt = this.uploadedAt.toString(),
    )
