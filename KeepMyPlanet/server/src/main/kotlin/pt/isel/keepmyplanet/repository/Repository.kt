package pt.isel.keepmyplanet.repository

interface Repository<T : Any, ID : Any> {
    suspend fun create(entity: T): T

    suspend fun getById(id: ID): T?

    suspend fun getAll(): List<T>

    suspend fun update(entity: T): T

    suspend fun deleteById(id: ID): Boolean
}
