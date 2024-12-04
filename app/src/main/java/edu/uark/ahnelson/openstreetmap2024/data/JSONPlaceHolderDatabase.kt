package edu.uark.ahnelson.openstreetmap2024.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import edu.uark.ahnelson.openstreetmap2024.Repository.Converters
import edu.uark.ahnelson.openstreetmap2024.data.entity.MintedToken
import edu.uark.ahnelson.openstreetmap2024.data.entity.Pin
import edu.uark.ahnelson.openstreetmap2024.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Pin::class, User::class, MintedToken::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Register the converters here
abstract class JSONPlaceHolderDatabase : RoomDatabase() {

    abstract fun jsonPlaceHolderDao(): JSONPlaceHolderDao

    private class JSONPlaceHolderDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val jsonDao = database.jsonPlaceHolderDao()
                    // Delete all content here.
                    jsonDao.deleteAll()

                    // Optionally, add any initial data here if needed.
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: JSONPlaceHolderDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): JSONPlaceHolderDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JSONPlaceHolderDatabase::class.java,
                    "json_database"
                )
                    .addCallback(JSONPlaceHolderDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
