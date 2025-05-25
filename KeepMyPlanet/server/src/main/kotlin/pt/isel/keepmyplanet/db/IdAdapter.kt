package pt.isel.keepmyplanet.db

import app.cash.sqldelight.ColumnAdapter
import pt.isel.keepmyplanet.domain.common.Id

object IdAdapter : ColumnAdapter<Id, Long> {
    override fun decode(databaseValue: Long) = Id(databaseValue.toUInt())

    override fun encode(value: Id) = value.value.toLong()
}
