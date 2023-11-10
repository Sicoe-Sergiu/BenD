package com.example.bend.utils
import androidx.room.TypeConverter
import java.sql.Time

class TimeConverter {
    @TypeConverter
    fun fromSqlTime(time: Time?): Long? {
        return time?.time
    }

    @TypeConverter
    fun toSqlTime(timeMillis: Long?): Time? {
        return timeMillis?.let { Time(it) }
    }
}
