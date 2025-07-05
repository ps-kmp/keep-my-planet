package pt.isel.keepmyplanet.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import org.koin.compose.koinInject
import org.koin.core.parameter.ParametersDefinition

@Composable
inline fun <reified T : BaseViewModel<*>> koinViewModel(
    noinline parameters: ParametersDefinition? = null,
): T {
    val vm =
        if (parameters == null) {
            koinInject<T>()
        } else {
            koinInject<T>(parameters = parameters)
        }

    DisposableEffect(vm) {
        onDispose {
            vm.onCleared()
        }
    }
    return vm
}
