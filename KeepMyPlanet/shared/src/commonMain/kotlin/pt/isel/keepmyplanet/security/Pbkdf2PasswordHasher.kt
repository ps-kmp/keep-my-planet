package pt.isel.keepmyplanet.security

import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.domain.user.PasswordHash

class Pbkdf2PasswordHasher(
    private val iterations: Int = 310_000,
    private val saltSize: Int = 16,
    private val keySize: Int = 32,
    private val digest: CryptographyAlgorithmId<Digest> = SHA256,
) : PasswordHasher {
    private val pbkdf2: PBKDF2 = CryptographyProvider.Companion.Default.get(PBKDF2.Companion)

    override suspend fun hash(password: Password): PasswordHash {
        val salt = CryptographyRandom.Default.nextBytes(saltSize)
        val dk =
            pbkdf2
                .secretDerivation(digest, iterations, keySize.bytes, salt)
                .deriveSecretToByteArray(password.value.encodeToByteArray())
        // format: iterations:salt:dk
        val result =
            listOf(
                iterations.toString(),
                salt.encodeBase64(),
                dk.encodeBase64(),
            ).joinToString(":")
        return PasswordHash(result)
    }

    override suspend fun verify(
        password: Password,
        storedHash: PasswordHash,
    ): Boolean {
        val (iterStr, saltB64, dkB64) =
            storedHash.value
                .split(":", limit = 3)
                .takeIf { it.size == 3 }
                ?: return false

        val iter = iterStr.toIntOrNull() ?: return false
        val (salt, expected) =
            runCatching {
                saltB64.decodeBase64Bytes() to dkB64.decodeBase64Bytes()
            }.getOrNull() ?: return false

        val derived =
            pbkdf2
                .secretDerivation(digest, iter, expected.size.bytes, salt)
                .deriveSecretToByteArray(password.value.encodeToByteArray())

        return derived.contentEquals(expected)
    }
}
