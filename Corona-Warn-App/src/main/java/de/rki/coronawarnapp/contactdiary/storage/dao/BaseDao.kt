package de.rki.coronawarnapp.contactdiary.storage.dao

import kotlinx.coroutines.flow.Flow

interface BaseDao<T> {
    suspend fun insert(entity: T)
    suspend fun insertAll(entities: List<T>)
    suspend fun update(entity: T)
    suspend fun updateAll(entities: List<T>)
    suspend fun delete(entity: T)
    suspend fun deleteAll()
    fun allEntries(): Flow<T>
}
