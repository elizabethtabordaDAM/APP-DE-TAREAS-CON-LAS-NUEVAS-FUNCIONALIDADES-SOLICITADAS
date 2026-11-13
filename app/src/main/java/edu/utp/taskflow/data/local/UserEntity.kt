package edu.utp.taskflow.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String,

    @ColumnInfo(defaultValue = "''")
    val email: String = "",

    val passwordHash: String,

    val salt: String
)