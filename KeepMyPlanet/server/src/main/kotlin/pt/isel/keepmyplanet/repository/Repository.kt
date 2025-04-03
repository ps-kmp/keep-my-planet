package pt.isel.keepmyplanet.repository

import kotlinx.coroutines.flow.Flow

interface Repository<T, ID> {
    suspend fun create(entity: T): T

    suspend fun getById(id: ID): T?

    fun getAll(): Flow<List<T>>

    suspend fun update(entity: T): T

    suspend fun deleteById(id: ID): Boolean
}
