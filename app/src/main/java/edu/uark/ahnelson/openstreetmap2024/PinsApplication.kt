package edu.uark.ahnelson.openstreetmap2024

import android.app.Application
import edu.uark.ahnelson.openstreetmap2024.Repository.PinRepository
import edu.uark.ahnelson.openstreetmap2024.Repository.PinRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PinsApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { PinRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { PinRepository(database.pinDao()) }
}
