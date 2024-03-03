package com.example.bend

import android.app.Application
import androidx.room.Room
//import com.example.bend.persistance.dao.AppDAO
import com.google.firebase.FirebaseApp


class BenDApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}