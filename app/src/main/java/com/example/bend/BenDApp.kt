package com.example.bend

import android.app.Application
import androidx.room.Room
import com.example.bend.persistance.dao.AppDAO
import com.example.bend.persistence.AppDatabase


class BenDApp : Application() {
    private var db : AppDatabase? = null;

    init {
        instance = this;
    }

    private fun getDb() : AppDatabase {
        if (db != null){
            return db!!;
        }else{
            db = Room.databaseBuilder(
                instance!!.applicationContext,
                AppDatabase::class.java, "BenD_db"
            ).fallbackToDestructiveMigration().build()
        }
        return db!!
    }

    companion object {
        private var instance: BenDApp? = null;

        fun getAppDao() : AppDAO {
            return instance!!.getDb().AppDAO();
        }
    }
}