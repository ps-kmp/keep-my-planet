package pt.isel.keepmyplanet.service

interface FileStorageService {
    suspend fun uploadImage(
        imageData: ByteArray,
        filename: String,
    ): Result<String>
}
