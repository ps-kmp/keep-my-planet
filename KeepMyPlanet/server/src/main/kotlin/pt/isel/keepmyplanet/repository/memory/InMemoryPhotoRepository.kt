package pt.isel.keepmyplanet.repository.memory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Photo
import pt.isel.keepmyplanet.repository.PhotoRepository
import pt.isel.keepmyplanet.utils.now

class InMemoryPhotoRepository : PhotoRepository {
    private val photos = ConcurrentHashMap<Id, Photo>()
    private val nextId = AtomicInteger(1)

    override suspend fun create(entity: Photo): Photo {
        val newId = Id(nextId.getAndIncrement().toUInt())
        val photoWithId = entity.copy(id = newId, uploadedAt = now())
        photos[newId] = photoWithId
        return photoWithId
    }

    override suspend fun getById(id: Id): Photo? = photos[id]

    override suspend fun getAll(
        limit: Int,
        offset: Int,
    ): List<Photo> =
        photos.values
            .sortedBy { it.id.value }
            .drop(offset)
            .take(limit)

    override suspend fun update(entity: Photo): Photo =
        throw UnsupportedOperationException("Photos are immutable in this system.")

    override suspend fun deleteById(id: Id): Boolean = photos.remove(id) != null

    fun clear() {
        photos.clear()
        nextId.set(1)
    }
}
