package pt.isel.keepmyplanet.di

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import org.koin.core.module.Module
import org.koin.dsl.module

private object NoOpSqlDriver : SqlDriver {
    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> {
        val emptyCursor =
            object : SqlCursor {
                override fun next(): QueryResult<Boolean> = QueryResult.Value(false)

                override fun getBoolean(index: Int): Boolean? = null

                override fun getBytes(index: Int): ByteArray? = null

                override fun getDouble(index: Int): Double? = null

                override fun getString(index: Int): String? = null

                override fun getLong(index: Int): Long? = null
            }
        return mapper(emptyCursor)
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> = QueryResult.Value(0L)

    override fun newTransaction(): QueryResult<Transacter.Transaction> {
        val fakeTransaction =
            object : Transacter.Transaction() {
                override val enclosingTransaction: Transacter.Transaction? = null

                override fun endTransaction(successful: Boolean): QueryResult<Unit> =
                    QueryResult.Value(Unit)
            }
        return QueryResult.Value(fakeTransaction)
    }

    override fun currentTransaction(): Transacter.Transaction? = null

    override fun addListener(
        vararg queryKeys: String,
        listener: Query.Listener,
    ) = Unit

    override fun removeListener(
        vararg queryKeys: String,
        listener: Query.Listener,
    ) = Unit

    override fun notifyListeners(vararg queryKeys: String) = Unit

    override fun close() = Unit
}

internal actual fun createDriverModule(): Module =
    module {
        single<SqlDriver> { NoOpSqlDriver }
    }
