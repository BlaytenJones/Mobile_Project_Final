package edu.uark.ahnelson.openstreetmap2024.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "mintedtoken_table")
data class MintedToken(
    @Expose @SerializedName("tokenId") @ColumnInfo(name="tokenId") val tokenId: Int,
    @Expose @SerializedName("mintNum") @ColumnInfo(name="mintNum") val mintNum: Int,
):Serializable