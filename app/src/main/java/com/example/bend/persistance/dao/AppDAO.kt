package com.example.bend.persistance.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.Event_Artist
import java.util.UUID
//@Dao
//interface AppDAO {
//    //Regular User
//
//    @Query("select * from regular_users where regular_users.uuid = :uuid")
//    suspend fun getRegularUserByUUID(uuid: UUID) : RegularUser?
//
//    @Query("select * from regular_users")
//    fun getRegularUsers() : LiveData<List<RegularUser>>
//
//    @Delete
//    fun deleteRegularUser(regularUser: RegularUser)
//
//    @Update
//    fun updateRegularUser(regularUser: RegularUser)
//
//    @Insert
//    fun insertRegularUser(regularUser: RegularUser)
//
//    //Artist
//
//    @Query("select * from artist where artist.uuid = :uuid")
//    suspend fun getArtistByUUID(uuid: UUID) : Artist?
//
//    @Query("select * from artist")
//    fun getArtists() : LiveData<List<Artist>>
//
//    @Delete
//    fun deleteArtist(artist: Artist)
//
//    @Update
//    fun updateArtist(artist: Artist)
//
//    @Insert
//    fun insertArtist(artist: Artist)
//
//    //Event
//
//    @Query("select * from event where event.uuid = :uuid")
//    suspend fun getEventByUUID(uuid: UUID) : Event?
//
//    @Query("select * from event")
//    fun getEvents() : LiveData<List<Event>>
//
//    @Delete
//    fun deleteEvent(event: Event)
//
//    @Update
//    fun updateEvent(event: Event)
//
//    @Insert
//    fun insertEvent(event: Event)
//
//    //EventFounder
//
//    @Query("select * from event_founder where event_founder.uuid = :uuid")
//    suspend fun getEventFounderByUUID(uuid: UUID) : EventFounder?
//
//    @Query("select * from event_founder")
//    fun getEventFounders() : LiveData<List<EventFounder>>
//
//    @Delete
//    fun deleteEventFounder(event_founder: EventFounder)
//
//    @Update
//    fun updateEventFounder(event_founder: EventFounder)
//
//    @Insert
//    fun insertEventFounder(event_founder: EventFounder)
//
//    //Event - Artist
//
//    @Query("select * from event_artist where event_artist.uuid = :uuid")
//    suspend fun getEvent_ArtistByUUID(uuid: UUID) : Event_Artist?
//
//    @Query("select * from event_artist")
//    fun getEvent_Artists() : LiveData<List<Event_Artist>>
//
//    @Delete
//    fun deleteEvent_Artist(event_artist: Event_Artist)
//
//    @Update
//    fun updateEvent_Artist(event_artist: Event_Artist)
//
//    @Insert
//    fun insertEvent_Artist(event_artist: Event_Artist)
//}