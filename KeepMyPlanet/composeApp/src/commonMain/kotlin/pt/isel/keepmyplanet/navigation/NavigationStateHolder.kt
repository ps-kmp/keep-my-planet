package pt.isel.keepmyplanet.navigation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scroll
import ovh.plrapps.mapcompose.api.setScroll
import ovh.plrapps.mapcompose.ui.state.MapState

data class ScrollPosition(
    val index: Int,
    val offset: Int,
)

data class MapPosition(
    val scale: Double,
    val scrollX: Double,
    val scrollY: Double,
)

object NavigationStateHolder {
    private val lazyListStates = mutableMapOf<String, ScrollPosition>()
    private val scrollStates = mutableMapOf<String, Int>()
    private val mapStates = mutableMapOf<String, MapPosition>()

    fun saveLazyListState(
        key: String,
        state: LazyListState,
    ) {
        lazyListStates[key] =
            ScrollPosition(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset)
    }

    fun restoreLazyListState(
        key: String,
        state: LazyListState,
        scope: CoroutineScope,
    ) {
        lazyListStates[key]?.let {
            scope.launch {
                state.scrollToItem(it.index, it.offset)
            }
        }
    }

    fun saveScrollState(
        key: String,
        state: ScrollState,
    ) {
        scrollStates[key] = state.value
    }

    fun restoreScrollState(
        key: String,
        state: ScrollState,
        scope: CoroutineScope,
    ) {
        scrollStates[key]?.let {
            scope.launch {
                state.scrollTo(it)
            }
        }
    }

    fun saveMapState(
        key: String,
        state: MapState,
    ) {
        mapStates[key] = MapPosition(state.scale, state.scroll.x, state.scroll.y)
    }

    fun restoreMapState(
        key: String,
        state: MapState,
        scope: CoroutineScope,
    ) {
        mapStates[key]?.let {
            scope.launch {
                state.setScroll(it.scrollX, it.scrollY)
                state.scale = it.scale
            }
        }
    }
}

@Composable
fun rememberSavableLazyListState(key: String): LazyListState {
    val scrollState =
        androidx.compose.foundation.lazy
            .rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key) {
        NavigationStateHolder.restoreLazyListState(key, scrollState, scope)
    }
    DisposableEffect(key) {
        onDispose {
            NavigationStateHolder.saveLazyListState(key, scrollState)
        }
    }
    return scrollState
}

@Composable
fun rememberSavableScrollState(key: String): ScrollState {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key) {
        NavigationStateHolder.restoreScrollState(key, scrollState, scope)
    }
    DisposableEffect(key) {
        onDispose {
            NavigationStateHolder.saveScrollState(key, scrollState)
        }
    }
    return scrollState
}
