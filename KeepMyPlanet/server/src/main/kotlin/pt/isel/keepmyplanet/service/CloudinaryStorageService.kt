package pt.isel.keepmyplanet.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import io.ktor.server.config.ApplicationConfig
import java.util.UUID
import pt.isel.keepmyplanet.exception.InternalServerException

class CloudinaryStorageService(
    config: ApplicationConfig,
) : FileStorageService {
    private val cloudinary: Cloudinary

    init {
        val cloudinaryUrl = config.property("cloudinary.url").getString()
        cloudinary = Cloudinary(cloudinaryUrl)
        cloudinary.config.secure = true
    }

    override suspend fun uploadImage(
        imageData: ByteArray,
        filename: String,
    ): Result<String> =
        runCatching {
            val publicId = "${UUID.randomUUID()}-${filename.substringBeforeLast('.')}"

            val options =
                ObjectUtils.asMap(
                    "public_id",
                    publicId,
                    "folder",
                    "profile_pictures",
                    "resource_type",
                    "image",
                    "overwrite",
                    true,
                )

            val uploadResult = cloudinary.uploader().upload(imageData, options)

            @Suppress("CAST_NEVER_SUCCEEDS")
            uploadResult["secure_url"] as? String
                ?: throw InternalServerException("Cloudinary did not return a secure URL.")
        }
}
