package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface JSONPlaceHolderDao {
    @MapInfo(keyColumn = "id")
    @Query("SELECT * FROM pin_table ORDER BY date ASC")
    fun getOrderedPins(): Flow<Map<Int, Pin>>

    @Update
    suspend fun update(pin: Pin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToken(mintedToken: MintedToken)

    @Transaction
    suspend fun addToken(user: User, mintedToken: MintedToken) {
        insertUser(user)
        insertToken(mintedToken)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pin: Pin)

    @Query("DELETE FROM pin_table WHERE localId = :pinId")
    suspend fun delete(pinId: Int)

    @Query("DELETE FROM pin_table")
    suspend fun deleteAll()
}
