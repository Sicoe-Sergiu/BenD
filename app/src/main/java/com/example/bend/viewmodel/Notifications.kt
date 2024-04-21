package com.example.bend.viewmodel

import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bend.Constants
import com.example.bend.model.Artist
import com.example.bend.model.Event
import com.example.bend.model.EventArtist
import com.example.bend.model.Followers
import com.example.bend.model.Notification
import com.example.bend.model.UserEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class Notifications {
    companion object {
        suspend fun sendNotification(
            toUserUUID: String,
            fromUserUUID: String,
            text: String,
            eventUUID: String = "",
            sensitive: Boolean = false
        ) {
            try {
                val notification = Notification(
                    uuid = UUID.randomUUID().toString(),
                    fromUserUUID = fromUserUUID,
                    eventUUID = eventUUID,
                    toUserUUID = toUserUUID,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    sensitive = sensitive
                )
                FirebaseFirestore.getInstance()
                    .collection("notification")
                    .document(notification.uuid)
                    .set(notification)
                    .await()
                Log.d("NotificationSuccess", "Notification sent to: $toUserUUID")
            } catch (e: Exception) {
                Log.e("NotificationError", "Failed to send notification: ${e.localizedMessage}", e)
            }
        }

        suspend fun notifySingleUser(
            fromUser: String,
            toUserUUID: String,
            event: Event,
            notificationText: String,
            sensitive: Boolean = false
        ) {
            try {
                sendNotification(
                    toUserUUID = toUserUUID,
                    fromUserUUID = fromUser,
                    text = notificationText,
                    eventUUID = event.uuid,
                    sensitive = sensitive
                )
                Log.d(
                    "NotifySingleUser",
                    "Notification sent to single user: $toUserUUID for event: ${event.uuid}"
                )
            } catch (e: Exception) {
                Log.e(
                    "NotifySingleUserError",
                    "Error notifying single user: ${e.localizedMessage}",
                    e
                )
            }
        }

        suspend fun notifyAllFollowers(userUUID: String, event: Event, notificationText: String) {
            try {
                val followersSnapshot = FirebaseFirestore.getInstance().collection("followers")
                    .whereEqualTo("followedUserUUID", userUUID)
                    .get().await()

                followersSnapshot.documents.forEach { document ->
                    val follower = document.toObject(Followers::class.java) ?: return@forEach
                    sendNotification(
                        toUserUUID = follower.userUUID,
                        fromUserUUID = userUUID,
                        text = notificationText,
                        eventUUID = event.uuid
                    )
                }
                Log.d(
                    "NotifyAllFollowers",
                    "Notified all followers of user: $userUUID for event: ${event.uuid}"
                )
            } catch (e: Exception) {
                Log.e(
                    "NotifyAllFollowersError",
                    "Error notifying all followers: ${e.localizedMessage}",
                    e
                )
            }
        }

        suspend fun notifyAllAttendees(
            event: Event,
            notificationText: String,
            sensitive: Boolean = false
        ) {
            try {
                val attendeesSnapshot = FirebaseFirestore.getInstance().collection("user_event")
                    .whereEqualTo("eventUUID", event.uuid)
                    .get().await()

                attendeesSnapshot.documents.forEach { document ->
                    val attendee = document.toObject(UserEvent::class.java) ?: return@forEach
                    sendNotification(
                        toUserUUID = attendee.userUUID,
                        fromUserUUID = event.founderUUID,
                        text = notificationText,
                        eventUUID = event.uuid,
                        sensitive = sensitive
                    )
                }
                Log.d("NotifyAllAttendees", "All attendees notified for event: ${event.uuid}")
            } catch (e: Exception) {
                Log.e(
                    "NotifyAllAttendeesError",
                    "Error notifying all attendees: ${e.localizedMessage}",
                    e
                )
            }
        }

        suspend fun notifyFollowersOfNewEvent(db: FirebaseFirestore, event: Event) {
            try {
                val followersSnapshot = db.collection("followers")
                    .whereEqualTo("followedUserUUID", event.founderUUID)
                    .get().await()

                followersSnapshot.documents.forEach { document ->
                    val follow = document.toObject(Followers::class.java) ?: return@forEach
                    sendNotification(
                        toUserUUID = follow.userUUID,
                        fromUserUUID = event.founderUUID,
                        text = Constants.NEW_EVENT,
                        eventUUID = event.uuid
                    )
                }
                Log.d("NotifyNewEvent", "Successfully notified all followers of new event: ${event.uuid}")
            } catch (e: Exception) {
                Log.e("NotifyNewEventError", "Error notifying followers of new event: ${e.localizedMessage}", e)
            }
        }

        suspend fun notifyFollowersOfEventPerformance(
            db: FirebaseFirestore,
            artists: List<Artist>,
            event: Event
        ) {
            artists.forEach { artist ->
                try {
                    val followersSnapshot = db.collection("followers")
                        .whereEqualTo("followedUserUUID", artist.uuid)
                        .get().await()

                    followersSnapshot.documents.forEach { document ->
                        val follower = document.toObject(Followers::class.java) ?: return@forEach
                        sendNotification(
                            toUserUUID = follower.userUUID,
                            fromUserUUID = artist.uuid,
                            text = Constants.ARTIST_PERFORM,
                            eventUUID = event.uuid
                        )
                    }
                    Log.d("NotifyArtistPerformance", "Successfully notified followers of artist ${artist.uuid} for event: ${event.uuid}")
                } catch (e: Exception) {
                    Log.e("NotifyArtistPerformanceError", "Error notifying followers of artist performance: ${artist.uuid}, ${e.localizedMessage}", e)
                }
            }
        }

        suspend fun notifyArtistsOfEvent(
            event: Event,
            notificationText: String,
            sensitive: Boolean = false
        ) {
            try {
                val artistsSnapshot = FirebaseFirestore.getInstance().collection("event_artist")
                    .whereEqualTo("eventUUID", event.uuid)
                    .get().await()

                artistsSnapshot.documents.forEach { document ->
                    val artistEvent = document.toObject(EventArtist::class.java) ?: return@forEach
                    notifySingleUser(
                        toUserUUID = artistEvent.artistUUID,
                        fromUser = event.founderUUID,
                        event = event,
                        notificationText = notificationText,
                        sensitive = sensitive
                    )
                }
                Log.d("NotifyArtistsOfEvent", "All artists notified for event: ${event.uuid}")
            } catch (e: Exception) {
                Log.e(
                    "NotifyArtistsOfEventError",
                    "Error notifying artists: ${e.localizedMessage}",
                    e
                )
            }
        }


    }

}