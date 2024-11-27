package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {

    @MapInfo(keyColumn = "id")
    @Query("SELECT * FROM pin_table ORDER BY date ASC")
    fun getOrderedTasks(): Flow<Map<Int, Pin>>

    @Update
    suspend fun update(task: Pin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Pin)

    @Query("DELETE FROM pin_table WHERE id = :pinId")
    suspend fun delete(pinId: Int)

    @Query("SELECT tempID FROM pin_table WHERE id = :pinId")
    fun getTempId(pinId: Int): Int?

    @Query("DELETE FROM pin_table")
    suspend fun deleteAll()
}
