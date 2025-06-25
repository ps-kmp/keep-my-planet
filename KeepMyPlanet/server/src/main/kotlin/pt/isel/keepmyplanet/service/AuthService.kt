package pt.isel.keepmyplanet.service

import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Password
import pt.isel.keepmyplanet.dto.auth.LoginRequest
import pt.isel.keepmyplanet.dto.auth.LoginResponse
import pt.isel.keepmyplanet.exception.AuthenticationException
import pt.isel.keepmyplanet.mapper.user.toResponse
import pt.isel.keepmyplanet.repository.UserRepository
import pt.isel.keepmyplanet.security.PasswordHasher

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) {
    suspend fun login(request: LoginRequest): Result<LoginResponse> =
        runCatching {
            val email = Email(request.email)
            val password = Password(request.password)

            val user =
                userRepository.findByEmail(email)
                    ?: throw AuthenticationException("Invalid email or password.")

            if (!passwordHasher.verify(password, user.passwordHash)) {
                throw AuthenticationException("Invalid email or password.")
            }

            val token = jwtService.generateToken(user.id)
            LoginResponse(token = token, user = user.toResponse())
        }
}
