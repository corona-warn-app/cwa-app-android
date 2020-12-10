package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryDateDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryElementDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryDateEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementLocationXRef
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementPersonXRef
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.SecurityHelper
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject

@Database(
    entities = [
        ContactDiaryDateEntity::class,
        ContactDiaryLocationEntity::class,
        ContactDiaryPersonEntity::class,
        ContactDiaryElementPersonXRef::class,
        ContactDiaryElementLocationXRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class ContactDiaryDatabase : RoomDatabase() {

    abstract fun contactDiaryDateDao(): ContactDiaryDateDao
    abstract fun contactDiaryElementDao(): ContactDiaryElementDao
    abstract fun locationDao(): ContactDiaryLocationDao
    abstract fun personDao(): ContactDiaryPersonDao

    class Factory @Inject constructor(@AppContext private val ctx: Context) {
        fun create(): ContactDiaryDatabase = Room
            .databaseBuilder(ctx, ContactDiaryDatabase::class.java, CONTACT_DIARY_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .openHelperFactory(SupportFactory(SecurityHelper.getDBPassword()))
            .build()
    }

    companion object {
        private const val CONTACT_DIARY_DATABASE_NAME = "ContactDiary-db"
    }
}
