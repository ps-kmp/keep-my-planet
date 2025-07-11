package pt.isel.keepmyplanet.ui.app.states

import pt.isel.keepmyplanet.domain.user.UserSession
import pt.isel.keepmyplanet.navigation.AppRoute
import pt.isel.keepmyplanet.navigation.NavDirection
import pt.isel.keepmyplanet.ui.base.UiState

data class AppUiState(
    val userSession: UserSession? = null,
    val navStack: List<AppRoute> = emptyList(),
    val currentRoute: AppRoute? = null,
    val navDirection: NavDirection = NavDirection.REPLACE,
) : UiState
