package pt.isel.keepmyplanet.repository.database.mappers

import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.domain.common.Photo
import pt.isel.keepmyplanet.domain.common.Url
import pt.isel.keepmyplanet.domain.event.Event
import pt.isel.keepmyplanet.domain.event.EventStateChange
import pt.isel.keepmyplanet.domain.event.EventStateChangeDetails
import pt.isel.keepmyplanet.domain.event.Period
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.User
import pt.isel.keepmyplanet.domain.zone.Location
import pt.isel.keepmyplanet.domain.zone.Zone
import ptiselkeepmyplanetdb.Event_state_changes
import ptiselkeepmyplanetdb.Events
import ptiselkeepmyplanetdb.FindByEventIdWithUserName
import ptiselkeepmyplanetdb.GetAll
import ptiselkeepmyplanetdb.GetAllByEventId
import ptiselkeepmyplanetdb.GetAllBySenderId
import ptiselkeepmyplanetdb.GetById
import ptiselkeepmyplanetdb.GetSingleByEventIdAndSeqNum
import ptiselkeepmyplanetdb.Messages
import ptiselkeepmyplanetdb.Photos
import ptiselkeepmyplanetdb.Users
import ptiselkeepmyplanetdb.Zones

internal fun Users.toDomainUser(): User =
    User(
        id = this.id,
        name = this.name,
        email = this.email,
        passwordHash = this.password_hash,
        profilePictureId = this.profile_picture_id,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

internal fun Zones.toDomainZone(photoIds: Set<Id>): Zone =
    Zone(
        id = this.id,
        location = Location(latitude = this.latitude, longitude = this.longitude),
        description = this.description,
        reporterId = this.reporter_id,
        eventId = this.event_id,
        status = this.status,
        zoneSeverity = this.zone_severity,
        photosIds = photoIds,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

internal fun Events.toDomainEvent(participantIds: Set<Id>): Event =
    Event(
        id = this.id,
        title = this.title,
        description = this.description,
        period = Period(this.start_datetime, this.end_datetime),
        zoneId = this.zone_id,
        organizerId = this.organizer_id,
        status = this.status,
        maxParticipants = this.max_participants,
        participantsIds = participantIds,
        createdAt = this.created_at,
        updatedAt = this.updated_at,
    )

internal fun GetById.toDomain(): Message =
    Message(
        id = this.id,
        eventId = this.event_id,
        senderId = this.sender_id,
        senderName = this.sender_name,
        content = this.content,
        timestamp = this.timestamp,
        chatPosition = this.chat_position,
    )

internal fun GetAllByEventId.toDomain(): Message =
    Message(
        id = this.id,
        eventId = this.event_id,
        senderId = this.sender_id,
        senderName = this.sender_name,
        content = this.content,
        timestamp = this.timestamp,
        chatPosition = this.chat_position,
    )

internal fun GetSingleByEventIdAndSeqNum.toDomain(): Message =
    Message(
        id = this.id,
        eventId = this.event_id,
        senderId = this.sender_id,
        senderName = this.sender_name,
        content = this.content,
        timestamp = this.timestamp,
        chatPosition = this.chat_position,
    )

internal fun GetAll.toDomain(): Message =
    Message(
        id = this.id,
        eventId = this.event_id,
        senderId = this.sender_id,
        senderName = this.sender_name,
        content = this.content,
        timestamp = this.timestamp,
        chatPosition = this.chat_position,
    )

internal fun GetAllBySenderId.toDomain(): Message =
    Message(
        id = this.id,
        eventId = this.event_id,
        senderId = this.sender_id,
        senderName = this.sender_name,
        content = this.content,
        timestamp = this.timestamp,
        chatPosition = this.chat_position,
    )

internal fun Messages.toDomainMessage(senderName: Name): Message =
    Message(
        id = this.id,
        eventId = this.event_id,
        senderId = this.sender_id,
        senderName = senderName,
        content = this.content,
        timestamp = this.timestamp,
        chatPosition = this.chat_position,
    )

internal fun Event_state_changes.toDomain(): EventStateChange =
    EventStateChange(
        id = this.id,
        eventId = this.event_id,
        newStatus = this.new_status,
        changedBy = this.changed_by,
        changeTime = this.change_time,
    )

internal fun FindByEventIdWithUserName.toEventStateChangeDetails(): EventStateChangeDetails =
    EventStateChangeDetails(
        stateChange =
            EventStateChange(
                id = this.id,
                eventId = this.event_id,
                newStatus = this.new_status,
                changedBy = this.changed_by,
                changeTime = this.change_time,
            ),
        changedByName = this.changed_by_name,
    )

internal fun Photos.toDomainPhoto(): Photo =
    Photo(
        id = this.id,
        url = Url(this.url),
        uploaderId = this.uploader_id,
        uploadedAt = this.uploaded_at,
    )
