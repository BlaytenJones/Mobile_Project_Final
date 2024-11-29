package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "pin_table")
data class Pin(
    @PrimaryKey @ColumnInfo(name="localId") val localId: Int?,
    @Expose @ColumnInfo(name="filepath") val filepath:String,
    @Expose @ColumnInfo(name="desc") val desc:String,
    @Expose @ColumnInfo(name="date") val date:String,
    @Expose @ColumnInfo(name="lat") val lat:Double,
    @Expose @ColumnInfo(name="lon") val lon:Double,
    @Expose @ColumnInfo(name="QRCode") val QRCode:String,
    @Expose @ColumnInfo(name = "id") val id: Int?,
    @Expose @ColumnInfo(name="uid") val uid:Int,
) {
}