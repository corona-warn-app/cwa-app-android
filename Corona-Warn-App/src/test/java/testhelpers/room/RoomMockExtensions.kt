package testhelpers.room

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.mockkStatic
import io.mockk.slot

fun RoomDatabase.mockDefaultOperations() {
    mockkStatic("androidx.room.RoomDatabaseKt")
    val transaction = slot<suspend () -> Unit>()
    coEvery { any<RoomDatabase>().withTransaction(capture(transaction)) } coAnswers {
        transaction.captured.invoke()
    }
}
