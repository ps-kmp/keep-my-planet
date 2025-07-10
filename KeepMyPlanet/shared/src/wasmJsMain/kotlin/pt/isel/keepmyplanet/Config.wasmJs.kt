package pt.isel.keepmyplanet

@JsName("API_BASE_URL")
private external val apiBaseUrl: String

actual val BASE_URL: String = apiBaseUrl
