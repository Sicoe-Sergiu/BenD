package com.example.bend.model

import androidx.room.ColumnInfo
import java.util.UUID
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "regular_users", indices = [Index(value = ["uuid"], unique = true)])
open class RegularUser(
    @PrimaryKey() val uuid: UUID,

    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "first_name") val first_name: String,
    @ColumnInfo(name = "last_name") val last_name: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String
)