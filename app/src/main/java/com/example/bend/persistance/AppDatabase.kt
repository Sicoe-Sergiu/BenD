package com.example.bend.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventFounder
import com.example.bend.model.Event_Artist
import com.example.bend.model.RegularUser
import com.example.bend.persistance.dao.AppDAO
import com.example.bend.utils.TimeConverter

@Database(entities = [RegularUser::class, Artist::class, Event::class, Event_Artist::class, EventFounder::class], version = 22, exportSchema = false)
@TypeConverters(TimeConverter::class)
public abstract class AppDatabase : RoomDatabase(){
    abstract fun AppDAO():AppDAO
}