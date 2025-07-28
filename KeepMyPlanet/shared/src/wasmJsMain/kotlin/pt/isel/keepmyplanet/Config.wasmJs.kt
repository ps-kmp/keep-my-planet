package pt.isel.keepmyplanet

private external val KMP_API_BASE_URL: String

actual val BASE_URL: String =
    if (KMP_API_BASE_URL.isNotBlank() && KMP_API_BASE_URL != "__API_BASE_URL__") {
        KMP_API_BASE_URL
    } else {
        "https://kmp-api.onrender.com"
    }
