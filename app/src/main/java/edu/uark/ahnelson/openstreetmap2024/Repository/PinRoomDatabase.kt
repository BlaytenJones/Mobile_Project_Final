package edu.uark.ahnelson.openstreetmap2024.Repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Pin class
@Database(entities = arrayOf(Pin::class), version = 1, exportSchema = false)
public abstract class PinRoomDatabase : RoomDatabase() {

    abstract fun pinDao(): PinDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: PinRoomDatabase? = null

        fun getDatabase(context: Context, scope:CoroutineScope): PinRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PinRoomDatabase::class.java,
                    "pin_database"
                ).addCallback(PinDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
    private class PinDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    resetDatabase(database.pinDao())
                }
            }
        }

        suspend fun resetDatabase(pinDao: PinDao) {
            // Delete all content here.
            pinDao.deleteAll()
        }
    }
}



