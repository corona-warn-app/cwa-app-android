package de.rki.coronawarnapp.contactdiary.storage.dao

import kotlinx.coroutines.flow.Flow

interface BaseDao<in T, out U> {
    suspend fun insert(entity: T): Long
    suspend fun insert(entities: List<T>)
    suspend fun update(entity: T)
    suspend fun update(entities: List<T>)
    suspend fun delete(entity: T)
    suspend fun delete(entities: List<T>)
    suspend fun deleteAll()
    suspend fun entityForId(id: Long): U
    fun allEntries(): Flow<List<U>>
}
