package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "pin_table")
data class Pin(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="filepath") val filepath:String,
    @ColumnInfo(name="desc") val desc:String,
    @ColumnInfo(name="date") val date:String,
    @ColumnInfo(name="latitude") val lat:Double,
    @ColumnInfo(name="longitude") val lon:Double,
    @ColumnInfo(name="tempID") val tempID:Int,
) {
}