package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationVisitDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonEncounterDao
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        ContactDiaryLocationEntity::class,
        ContactDiaryLocationVisitEntity::class,
        ContactDiaryPersonEntity::class,
        ContactDiaryPersonEncounterEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class ContactDiaryDatabase : RoomDatabase() {

    abstract fun locationDao(): ContactDiaryLocationDao
    abstract fun locationVisitDao(): ContactDiaryLocationVisitDao
    abstract fun personDao(): ContactDiaryPersonDao
    abstract fun personEncounterDao(): ContactDiaryPersonEncounterDao

    class Factory @Inject constructor(@AppContext private val ctx: Context) {
        fun create(): ContactDiaryDatabase = Room
            .databaseBuilder(ctx, ContactDiaryDatabase::class.java, CONTACT_DIARY_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        private const val CONTACT_DIARY_DATABASE_NAME = "ContactDiary-db"
    }
}
