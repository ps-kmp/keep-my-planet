package pt.isel.keepmyplanet.data.model

data class UserSession(
    val username: String,
    // val userId: UInt, // valor será obtido do servidor após o login
    val eventId: UInt, // valor será obtido do servidor após entrar num evento
)
