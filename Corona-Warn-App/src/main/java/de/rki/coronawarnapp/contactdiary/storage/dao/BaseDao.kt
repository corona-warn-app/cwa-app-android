package de.rki.coronawarnapp.contactdiary.storage.dao

import kotlinx.coroutines.flow.Flow

interface BaseDao<T> {
    suspend fun insert(entity: T)
    suspend fun insert(entities: List<T>)
    suspend fun update(entity: T)
    suspend fun update(entities: List<T>)
    suspend fun delete(entity: T)
    suspend fun delete(entities: List<T>)
    suspend fun deleteAll()
    suspend fun entityForId(id: Long): T
    fun allEntries(): Flow<List<T>>
}
