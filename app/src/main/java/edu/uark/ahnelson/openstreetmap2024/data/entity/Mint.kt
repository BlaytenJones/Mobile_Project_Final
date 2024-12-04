package edu.uark.ahnelson.openstreetmap2024.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "mint_table")
data class Mint(
    @PrimaryKey @ColumnInfo(name="localId") val localId: Int?,
    @Expose @ColumnInfo(name="filepath") val filepath:String,
) {
}