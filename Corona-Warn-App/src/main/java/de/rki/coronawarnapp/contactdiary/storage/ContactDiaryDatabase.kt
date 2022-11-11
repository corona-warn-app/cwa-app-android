package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationVisitDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonEncounterDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryCoronaTestDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiarySubmissionDao
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiarySubmissionEntity
import de.rki.coronawarnapp.contactdiary.storage.internal.converters.ContactDiaryRoomConverters
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration1To2
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration2To3
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration3To4
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        ContactDiaryLocationEntity::class,
        ContactDiaryLocationVisitEntity::class,
        ContactDiaryPersonEntity::class,
        ContactDiaryPersonEncounterEntity::class,
        ContactDiaryCoronaTestEntity::class,
        ContactDiarySubmissionEntity::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 4,
            to = 5
        )
    ]
)
@TypeConverters(CommonConverters::class, ContactDiaryRoomConverters::class)
abstract class ContactDiaryDatabase : RoomDatabase() {

    abstract fun locationDao(): ContactDiaryLocationDao
    abstract fun locationVisitDao(): ContactDiaryLocationVisitDao
    abstract fun personDao(): ContactDiaryPersonDao
    abstract fun personEncounterDao(): ContactDiaryPersonEncounterDao
    abstract fun coronaTestDao(): ContactDiaryCoronaTestDao
    abstract fun submissionDao(): ContactDiarySubmissionDao

    class Factory @Inject constructor(@AppContext private val ctx: Context) {
        fun create(databaseName: String = CONTACT_DIARY_DATABASE_NAME): ContactDiaryDatabase = Room
            .databaseBuilder(ctx, ContactDiaryDatabase::class.java, databaseName)
            .addMigrations(
                ContactDiaryDatabaseMigration1To2,
                ContactDiaryDatabaseMigration2To3,
                ContactDiaryDatabaseMigration3To4
            )
            .build()
    }

    companion object {
        private const val CONTACT_DIARY_DATABASE_NAME = "ContactDiary-db"
    }
}
