package pt.isel.keepmyplanet.data.http

import pt.isel.keepmyplanet.dto.common.ErrorResponse

class ApiException(
    val error: ErrorResponse,
) : Exception(error.message)
