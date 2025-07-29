package pt.isel.keepmyplanet.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import pt.isel.keepmyplanet.data.api.PhotoApi
import pt.isel.keepmyplanet.data.cache.PhotoCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.dto.photo.PhotoResponse

class PhotoApiRepository(
    private val photoApi: PhotoApi,
    private val photoCache: PhotoCacheRepository?,
    private val httpClient: HttpClient,
) {
    suspend fun createPhoto(
        imageData: ByteArray,
        filename: String,
    ): Result<PhotoResponse> {
        val networkResult = photoApi.createPhoto(imageData, filename)
        networkResult.onSuccess { photoResponse ->
            photoCache?.insertPhotoUrl(Id(photoResponse.id), photoResponse.url)
        }
        return networkResult
    }

    suspend fun getPhotoUrl(photoId: Id): Result<String> =
        runCatching {
            photoCache?.getPhotoUrl(photoId) ?: run {
                val networkResult = photoApi.getPhotoById(photoId)
                val url = networkResult.getOrThrow().url
                photoCache?.insertPhotoUrl(photoId, url)
                url
            }
        }

    suspend fun getPhotoModel(photoId: Id): Result<Any> {
        return runCatching {
            val cachedData = photoCache?.getPhotoData(photoId)
            if (cachedData != null) return@runCatching cachedData

            val url = getPhotoUrl(photoId).getOrThrow()
            val networkData = httpClient.get(url).readRawBytes()
            photoCache?.updatePhotoData(photoId, networkData)
            networkData
        }
    }
}
