package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_table")
data class Token(
    @PrimaryKey val tokenId: Int?,
    @ColumnInfo(name="filepath") val filepath:String,
) {
}