package pt.isel.keepmyplanet.db

import app.cash.sqldelight.ColumnAdapter
import pt.isel.keepmyplanet.domain.common.Description
import pt.isel.keepmyplanet.domain.event.Title
import pt.isel.keepmyplanet.domain.message.MessageContent
import pt.isel.keepmyplanet.domain.user.Email
import pt.isel.keepmyplanet.domain.user.Name
import pt.isel.keepmyplanet.domain.user.PasswordHash

object TitleAdapter : ColumnAdapter<Title, String> {
    override fun decode(databaseValue: String): Title = Title(databaseValue)

    override fun encode(value: Title): String = value.value
}

object DescriptionAdapter : ColumnAdapter<Description, String> {
    override fun decode(databaseValue: String): Description = Description(databaseValue)

    override fun encode(value: Description): String = value.value
}

object EmailAdapter : ColumnAdapter<Email, String> {
    override fun decode(databaseValue: String): Email = Email(databaseValue)

    override fun encode(value: Email): String = value.value
}

object PasswordHashAdapter : ColumnAdapter<PasswordHash, String> {
    override fun decode(databaseValue: String): PasswordHash = PasswordHash(databaseValue)

    override fun encode(value: PasswordHash): String = value.value
}

object NameAdapter : ColumnAdapter<Name, String> {
    override fun decode(databaseValue: String): Name = Name(databaseValue)

    override fun encode(value: Name): String = value.value
}

object MessageContentAdapter : ColumnAdapter<MessageContent, String> {
    override fun decode(databaseValue: String): MessageContent = MessageContent(databaseValue)

    override fun encode(value: MessageContent): String = value.value
}
