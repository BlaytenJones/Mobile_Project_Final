package edu.uark.ahnelson.openstreetmap2024.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "pin_table")
data class Pin(
    @PrimaryKey @SerializedName("localId") @ColumnInfo(name = "localId") val localId: Int? = null, // nullable and default
    @Expose @SerializedName("filepath") @ColumnInfo(name = "filepath") val filepath: String = "", // default value
    @Expose @SerializedName("desc") @ColumnInfo(name = "desc") val desc: String = "", // default value
    @Expose @SerializedName("date") @ColumnInfo(name = "date") val date: String = "", // default value
    @Expose @SerializedName("lat") @ColumnInfo(name = "lat") val lat: Double = 0.0, // default value
    @Expose @SerializedName("lon") @ColumnInfo(name = "lon") val lon: Double = 0.0, // default value
    @Expose @SerializedName("QRCode") @ColumnInfo(name = "QRCode") val QRCode: String = "", // default value
    @Expose @SerializedName("id") @ColumnInfo(name = "id") val id: Int? = null, // nullable and default
    @Expose @SerializedName("uid") @ColumnInfo(name = "uid") val uid: String = "" // default value
)