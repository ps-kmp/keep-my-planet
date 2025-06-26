package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Photo
import pt.isel.keepmyplanet.domain.common.Url
import pt.isel.keepmyplanet.exception.NotFoundException
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.utils.now

class PhotoService(
    private val photoRepository: PhotoRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService,
) {
    suspend fun createPhoto(
        imageData: ByteArray,
        filename: String,
        uploaderId: Id,
    ): Result<Photo> =
        runCatching {
            userRepository.getById(uploaderId)
                ?: throw NotFoundException("User '$uploaderId' not found.")

            val imageUrl = fileStorageService.uploadImage(imageData, filename).getOrThrow()

            val newPhoto =
                Photo(
                    id = Id(0U),
                    url = Url(imageUrl),
                    uploaderId = uploaderId,
                    uploadedAt = now(),
                )

            photoRepository.create(newPhoto)
        }

    suspend fun getPhotoDetails(photoId: Id): Result<Photo> =
        runCatching {
            photoRepository.getById(photoId)
                ?: throw NotFoundException("Photo with ID '${photoId.value}' not found.")
        }
}
