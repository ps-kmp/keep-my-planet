package pt.isel.keepmyplanet.repository.database

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Photo
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.repository.database.mappers.toDomainPhoto
import pt.isel.keepmyplanet.utils.now
import ptiselkeepmyplanetdb.PhotoQueries

class DatabasePhotoRepository(
    private val photoQueries: PhotoQueries,
) : PhotoRepository {
    override suspend fun create(entity: Photo): Photo =
        photoQueries.transactionWithResult {
            photoQueries
                .insert(
                    url = entity.url.value,
                    uploader_id = entity.uploaderId,
                    uploaded_at = now(),
                ).executeAsOne()
                .toDomainPhoto()
        }

    override suspend fun getById(id: Id): Photo? =
        photoQueries
            .getById(id)
            .executeAsOneOrNull()
            ?.toDomainPhoto()

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<Photo> = throw UnsupportedOperationException("Not yet implemented")

    override suspend fun update(entity: Photo): Photo =
        throw UnsupportedOperationException("Photos are immutable in this system.")

    override suspend fun deleteById(id: Id): Boolean =
        throw UnsupportedOperationException("Not yet implemented")
}
