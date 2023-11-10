package com.example.bend

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bend.model.Artist
import com.example.bend.model.EventFounder
import com.example.bend.model.RegularUser
import com.example.bend.persistance.dao.AppDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class UsersViewModel (
    private val db : AppDAO
): ViewModel() {

    val regular_users : LiveData<List<RegularUser>> = db.getRegularUsers()
    val artists : LiveData<List<Artist>> = db.getArtists()
    val event_founders : LiveData<List<EventFounder>> = db.getEventFounders()

//    Regular Users
    fun deleteRegularUser( user: RegularUser){
        viewModelScope.launch (Dispatchers.IO){
            db.deleteRegularUser(user)
        }
    }

    fun updateRegularUser( user: RegularUser){
        viewModelScope.launch (Dispatchers.IO){
            db.updateRegularUser(user)
        }
    }

    fun createRegularUser(username:String, first_name:String, last_name:String, email:String, password:String){
        val user = RegularUser(uuid = UUID.randomUUID(), username = username, first_name = first_name, last_name = last_name, email = email, password = password);
        viewModelScope.launch (Dispatchers.IO){
            db.insertRegularUser(user)
        }
    }

    suspend fun getRegularUser(userUUID: UUID): RegularUser? {
        return db.getRegularUserByUUID(userUUID)
    }

//    Artist
    fun deleteArtist( user: Artist){
        viewModelScope.launch (Dispatchers.IO){
            db.deleteArtist(user)
        }
    }

    fun updateArtist( user: Artist){
        viewModelScope.launch (Dispatchers.IO){
            db.updateArtist(user)
        }
    }

    fun createArtist(username:String, first_name:String, last_name:String, email:String, password:String, stage_name:String, rating: Float){
        val user = Artist(uuid = UUID.randomUUID(), username = username, first_name = first_name, last_name = last_name, email = email, password = password, stage_name = stage_name, rating = rating)
        viewModelScope.launch (Dispatchers.IO){
            db.insertArtist(user)
        }
    }

    suspend fun getArtist(userUUID: UUID): Artist? {
        return db.getArtistByUUID(userUUID)
    }

//    EventFounder
    fun deleteEventFounder( user: EventFounder){
        viewModelScope.launch (Dispatchers.IO){
            db.deleteEventFounder(user)
        }
    }

    fun updateRegularUser( user: EventFounder){
        viewModelScope.launch (Dispatchers.IO){
            db.updateEventFounder(user)
        }
    }

    fun createEventFounder(username:String, first_name:String, last_name:String, email:String, password:String, phone:String, rating:Float){
        val user = EventFounder(uuid = UUID.randomUUID(), username = username, first_name = first_name, last_name = last_name, email = email, password = password, phone = phone, rating = rating)
        viewModelScope.launch (Dispatchers.IO){
            db.insertEventFounder(user)
        }
    }

    suspend fun getEventFounder(userUUID: UUID): EventFounder? {
        return db.getEventFounderByUUID(userUUID)
    }
}

class RegularUserViewModelFactory (
    private val db: AppDAO
) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UsersViewModel(
            db = db
        ) as T
    }
}
