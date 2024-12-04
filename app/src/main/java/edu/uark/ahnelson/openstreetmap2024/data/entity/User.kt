package edu.uark.ahnelson.openstreetmap2024.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "user_table")
data class User(
    @Expose @SerializedName("email") @PrimaryKey @ColumnInfo(name = "email") val email: String,
    @Expose @SerializedName("uid") @ColumnInfo(name = "uid") val uid: String,
    @Expose @SerializedName("inventory") @ColumnInfo(name = "inventory") val inventory: List<MintedToken>,
    @Expose @SerializedName("solved") @ColumnInfo(name = "solved") val solved: List<Int>,
) : Serializable