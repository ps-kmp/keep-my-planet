package pt.isel.keepmyplanet.service

import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

class LocalFileStorageService(
    private val baseUrl: String,
    private val uploadDir: String = "uploads/images",
) : FileStorageService {
    init {
        Files.createDirectories(Paths.get(uploadDir))
    }

    override suspend fun uploadImage(
        imageData: ByteArray,
        filename: String,
    ): Result<String> =
        runCatching {
            val extension = filename.substringAfterLast('.', "")
            val uniqueFilename = "${UUID.randomUUID()}.$extension"
            val savePath = Paths.get(uploadDir, uniqueFilename)

            Files.write(savePath, imageData)

            "$baseUrl/static/images/$uniqueFilename"
        }
}
