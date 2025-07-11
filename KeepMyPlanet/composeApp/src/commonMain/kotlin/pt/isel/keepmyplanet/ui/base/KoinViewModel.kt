package pt.isel.keepmyplanet.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.koin.compose.getKoin
import org.koin.core.parameter.ParametersDefinition

@Composable
inline fun <reified T : BaseViewModel<*>> koinViewModel(
    noinline parameters: ParametersDefinition? = null,
): T {
    val koin = getKoin()
    val vm =
        remember(parameters) {
            koin.get<T>(parameters = parameters)
        }

    DisposableEffect(vm) {
        onDispose {
            vm.onCleared()
        }
    }
    return vm
}
