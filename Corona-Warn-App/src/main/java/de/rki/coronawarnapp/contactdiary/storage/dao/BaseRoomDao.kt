package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

abstract class BaseRoomDao<T> : BaseDao<T> {
    @Insert
    abstract override suspend fun insert(entity: T)

    @Insert
    abstract override suspend fun insertAll(entities: List<T>)

    @Update
    abstract override suspend fun update(entity: T)

    @Update
    abstract override suspend fun updateAll(entities: List<T>)

    @Delete
    abstract override suspend fun delete(entity: T)
}
