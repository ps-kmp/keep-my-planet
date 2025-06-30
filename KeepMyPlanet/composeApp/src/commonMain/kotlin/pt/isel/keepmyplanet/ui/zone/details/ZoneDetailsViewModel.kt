package pt.isel.keepmyplanet.ui.zone.details

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.data.api.PhotoApi
import pt.isel.keepmyplanet.data.api.ZoneApi
import pt.isel.keepmyplanet.data.repository.PhotoCacheRepository
import pt.isel.keepmyplanet.data.repository.ZoneCacheRepository
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.mapper.zone.toZone
import pt.isel.keepmyplanet.session.SessionManager
import pt.isel.keepmyplanet.ui.viewmodel.BaseViewModel
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsEvent
import pt.isel.keepmyplanet.ui.zone.details.states.ZoneDetailsUiState

class ZoneDetailsViewModel(
    private val zoneApi: ZoneApi,
    private val photoApi: PhotoApi,
    private val zoneCacheRepository: ZoneCacheRepository,
    private val photoCacheRepository: PhotoCacheRepository,
    private val sessionManager: SessionManager,
    private val httpClient: HttpClient,
) : BaseViewModel<ZoneDetailsUiState>(ZoneDetailsUiState()) {
    private val currentUser: UserInfo?
        get() = sessionManager.userSession.value?.userInfo

    override fun handleErrorWithMessage(message: String) {
        sendEvent(ZoneDetailsEvent.ShowSnackbar(message))
    }

    fun loadZoneDetails(zoneId: Id) {
        launchWithResult(
            onStart = {
                viewModelScope.launch {
                    val cachedZone = zoneCacheRepository.getZoneById(zoneId)
                    if (cachedZone != null) {
                        val photoModels =
                            cachedZone.photosIds.associateWith {
                                photoCacheRepository.getPhotoData(it)
                                    ?: photoCacheRepository.getPhotoUrl(it)
                                    ?: ""
                            }
                        setState {
                            copy(
                                zone = cachedZone,
                                photoModels = photoModels,
                                isLoading = true,
                            )
                        }
                    } else {
                        setState { copy(isLoading = true, zone = null, error = null) }
                    }
                }
                this
            },
            onFinally = { copy(isLoading = false) },
            block = { zoneApi.getZoneDetails(zoneId.value) },
            onSuccess = { response ->
                val zone = response.toZone()
                setState {
                    copy(
                        zone = zone,
                        canUserManageZone = zone.reporterId == currentUser?.id,
                    )
                }
                fetchAndCachePhotos(zone.photosIds)
                zoneCacheRepository.insertZones(listOf(zone))
            },
            onError = {
                if (currentState.zone == null) {
                    setState { copy(error = getErrorMessage("Failed to load zone details", it)) }
                } else {
                    handleErrorWithMessage(getErrorMessage("Failed to refresh zone details", it))
                }
            },
        )
    }

    private suspend fun fetchAndCachePhotos(photoIds: Set<Id>) {
        val urls =
            coroutineScope {
                photoIds.map { id -> async { photoApi.getPhotoById(id) } }.awaitAll()
            }
        val fetchedPhotoModels = mutableMapOf<Id, Any>()
        urls.forEach { result ->
            result.onSuccess { photo ->
                val photoId = Id(photo.id)
                photoCacheRepository.insertPhotoUrl(photoId, photo.url)
                fetchedPhotoModels[photoId] = photo.url
            }
        }
        setState { copy(photoModels = fetchedPhotoModels) }

        viewModelScope.launch {
            fetchedPhotoModels.forEach { (id, model) ->
                if (model is String) {
                    runCatching {
                        val imageData = httpClient.get(model).readRawBytes()
                        photoCacheRepository.updatePhotoData(id, imageData)
                    }
                }
            }
        }
    }

    fun deleteZone() {
        val zoneId = currentState.zone?.id ?: return

        launchWithResult(
            onStart = { copy(actionState = ZoneDetailsUiState.ActionState.DELETING) },
            onFinally = { copy(actionState = ZoneDetailsUiState.ActionState.IDLE) },
            block = { zoneApi.deleteZone(zoneId.value) },
            onSuccess = {
                sendEvent(ZoneDetailsEvent.ShowSnackbar("Zone deleted successfully"))
                sendEvent(ZoneDetailsEvent.ZoneDeleted)
            },
            onError = { handleErrorWithMessage(getErrorMessage("Failed to delete zone", it)) },
        )
    }
}
