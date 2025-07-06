package pt.isel.keepmyplanet.data.cache.mappers

import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.cache.EventCache
import pt.isel.keepmyplanet.cache.EventStatusHistoryCache
import pt.isel.keepmyplanet.cache.MessageCache
import pt.isel.keepmyplanet.cache.UserCache
import pt.isel.keepmyplanet.cache.ZoneCache
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.UserCacheInfo
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Radius
import pt.isel.keepmyplanet.domain.zone.Zone
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.dto.user.UserInfoSummaryResponse
import pt.isel.keepmyplanet.utils.safeValueOf

fun EventCache.toEvent(participantIds: Set<Id>): Event =
    Event(
        id = Id(id.toUInt()),
        title = Title(title),
        description = Description(description),
        period = Period(LocalDateTime.parse(startDate), endDate?.let { LocalDateTime.parse(it) }),
        zoneId = Id(zoneId.toUInt()),
        organizerId = Id(organizerId.toUInt()),
        status = safeValueOf<EventStatus>(status) ?: EventStatus.UNKNOWN,
        maxParticipants = maxParticipants?.toInt(),
        participantsIds = participantIds,
        pendingOrganizerId = pendingOrganizerId?.let { Id(it.toUInt()) },
        transferRequestTime = transferRequestTime?.let { LocalDateTime.parse(it) },
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt),
    )

fun EventStatusHistoryCache.toResponse(): EventStateChangeResponse =
    EventStateChangeResponse(
        id = id.toUInt(),
        eventId = event_id.toUInt(),
        newStatus = safeValueOf<EventStatus>(new_status) ?: EventStatus.UNKNOWN,
        changedBy = UserInfoSummaryResponse(changed_by_id.toUInt(), changed_by_name),
        changeTime = change_time,
    )

fun MessageCache.toMessage(): Message =
    Message(
        id = Id(id.toUInt()),
        eventId = Id(eventId.toUInt()),
        senderId = Id(senderId.toUInt()),
        senderName = Name(senderName),
        content = MessageContent(content),
        timestamp = LocalDateTime.parse(timestamp),
        chatPosition = chatPosition.toInt(),
    )

fun UserCache.toUserCacheInfo(): UserCacheInfo =
    UserCacheInfo(
        id = Id(id.toUInt()),
        name = Name(name),
        email = Email(email),
        profilePictureUrl = profilePictureUrl,
    )

fun ZoneCache.toZone(
    beforePhotosIds: Set<Id>,
    afterPhotosIds: Set<Id>,
): Zone =
    Zone(
        id = Id(id.toUInt()),
        location = Location(latitude, longitude),
        radius = Radius(this.radius),
        description = Description(description),
        reporterId = Id(reporterId.toUInt()),
        eventId = eventId?.let { Id(it.toUInt()) },
        status = safeValueOf<ZoneStatus>(status) ?: ZoneStatus.REPORTED,
        zoneSeverity = safeValueOf<ZoneSeverity>(zoneSeverity) ?: ZoneSeverity.UNKNOWN,
        beforePhotosIds = beforePhotosIds,
        afterPhotosIds = afterPhotosIds,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt),
    )
