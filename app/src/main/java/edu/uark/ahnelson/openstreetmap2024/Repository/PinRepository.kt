package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class PinRepository(private val taskDao: PinDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allPins: Flow<Map<Int, Pin>> = taskDao.getOrderedTasks()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(pin: Pin){
        taskDao.insert(pin)
    }

    @WorkerThread
    suspend fun update(task: Pin){
        taskDao.update(task)
    }

    @WorkerThread
    suspend fun delete(pinId: Int){
        taskDao.delete(pinId)
    }

    @WorkerThread
    suspend fun reset(){
        taskDao.deleteAll()
    }

    @WorkerThread
    fun getTempId(pinId: Int): Int? {
        return taskDao.getTempId(pinId)
    }
}
