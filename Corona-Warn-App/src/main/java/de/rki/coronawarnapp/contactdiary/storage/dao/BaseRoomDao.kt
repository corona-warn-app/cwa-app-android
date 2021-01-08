package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

abstract class BaseRoomDao<T, U> : BaseDao<T, U> {
    @Insert
    abstract override suspend fun insert(entity: T): Long

    @Insert
    abstract override suspend fun insert(entities: List<T>)

    @Update
    abstract override suspend fun update(entity: T)

    @Update
    abstract override suspend fun update(entities: List<T>)

    @Delete
    abstract override suspend fun delete(entity: T)

    @Delete
    abstract override suspend fun delete(entities: List<T>)
}
