package com.example.bend

import android.util.Log
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.Followers
import com.example.bend.model.UserEvent
import com.example.bend.view_models.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Notifications {
    companion object{
        suspend fun notifySingleUser(fromUser:String, toUserUUID: String, event: Event, notificationText: String, sensitive: Boolean = false){

            try {
                HomeViewModel.sendNotification(
                    toUserUUID = toUserUUID,
                    fromUserUUID = fromUser,
                    text = notificationText,
                    eventUUID = event.uuid,
                    sensitive = sensitive
                )
            } catch (e: Exception) {
                Log.e("EVENT", "Error notifying user", e)
            }
        }

        suspend fun notifyAllFollowers(userUUID: String, event: Event, notificationText: String){

            try {
                val followersSnapshot = FirebaseFirestore.getInstance().collection("followers")
                    .whereEqualTo("followedUserUUID", userUUID)
                    .get().await()

                for (document in followersSnapshot) {
                    val follow = document.toObject(Followers::class.java)
                    HomeViewModel.sendNotification(
                        toUserUUID = follow.userUUID,
                        fromUserUUID = event.founderUUID,
                        text = notificationText,
                        eventUUID = event.uuid
                    )
                }
            } catch (e: Exception) {
                Log.e("EVENT", "Error notifying followers", e)
            }
        }
        suspend fun notifyAllAttendees(event: Event, notificationText: String, sensitive: Boolean = false){

            try {

                val attendeesSnapshot = FirebaseFirestore.getInstance().collection("user_event")
                    .whereEqualTo("eventUUID", event.uuid)
                    .get().await()
                Log.e("INSIDE notifyAllAttendees", event.uuid)
                Log.e("INSIDE notifyAllAttendees", attendeesSnapshot.documents.toString())

                for (document in attendeesSnapshot) {
                    val attendee = document.toObject(UserEvent::class.java)
                    HomeViewModel.sendNotification(
                        toUserUUID = attendee.userUUID,
                        fromUserUUID = event.founderUUID,
                        text = notificationText,
                        eventUUID = event.uuid,
                        sensitive = sensitive
                    )
                }
            } catch (e: Exception) {
                Log.e("EVENT", "Error notifying attendees", e)
            }
        }

        suspend fun notifyFollowersOfNewEvent(db: FirebaseFirestore, event: Event) {
            try {
                val followersSnapshot = db.collection("followers")
                    .whereEqualTo("followedUserUUID", event.founderUUID)
                    .get().await()

                for (document in followersSnapshot) {
                    val follow = document.toObject(Followers::class.java) ?: continue
                    HomeViewModel.sendNotification(
                        toUserUUID = follow.userUUID,
                        fromUserUUID = event.founderUUID,
                        text = Constants.NEW_EVENT,
                        eventUUID = event.uuid
                    )
                }
            } catch (e: Exception) {
                Log.e("EVENT", "Error notifying followers", e)
            }
        }

        suspend fun notifyFollowersOfEventPerformance(db: FirebaseFirestore, artists: List<Artist>, event: Event) {
            artists.forEach { artist ->
                try {
                    val followersSnapshot = db.collection("followers")
                        .whereEqualTo("followedUserUUID", artist.uuid)
                        .get().await()

                    for (document in followersSnapshot.documents) {
                        val follower = document.toObject(Followers::class.java) ?: continue
                        HomeViewModel.sendNotification(
                            toUserUUID = follower.userUUID,
                            fromUserUUID = artist.uuid,
                            text = Constants.ARTIST_PERFORM,
                            eventUUID = event.uuid
                        )
                    }
                } catch (e: Exception) {
                    Log.e("EVENT", "Error notifying followers of artist performance", e)
                }
            }
        }

        suspend fun notifyArtistsOfEvent(event: Event, notificationText: String, sensitive: Boolean = false) {
            try {
                val artistsSnapshot = FirebaseFirestore.getInstance().collection("event_artist")
                    .whereEqualTo("eventUUID", event.uuid)
                    .get().await()

                for (document in artistsSnapshot) {
                    val artistEvent = document.toObject(EventArtist::class.java)

                    notifySingleUser(
                        toUserUUID = artistEvent.artistUUID,
                        fromUser = event.founderUUID,
                        notificationText = notificationText,
                        event = event,
                        sensitive = sensitive
                    )
                }
            } catch (e: Exception) {
                Log.e("EVENT", "Error notifying followers", e)
            }
        }
    }

}