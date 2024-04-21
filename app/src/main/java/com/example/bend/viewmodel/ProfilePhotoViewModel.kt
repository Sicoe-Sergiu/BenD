package com.example.bend.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bend.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfilePhotoViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser
    val errorMessages: LiveData<String> = MutableLiveData()

    private var photoUri: Uri? = null
    fun setPhoto(uri: Uri?) {
        photoUri = uri
    }


    fun uploadPhotoToFirebase(navController: NavController) {
        Log.d("LOG URI", photoUri.toString())

        if (photoUri == null) {
            navController.navigate(Constants.NAVIGATION_HOME_PAGE)
        } else {
            val storageRef: StorageReference =
                storage.reference.child("profile_photos/${currentUser?.uid}")

            photoUri?.let {
                storageRef.putFile(it)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            updateProfilePhotoUrlInAllCollections(
                                documentId = currentUser?.uid ?: "",
                                newProfilePhotoUrl = downloadUrl.toString(),
                                navController = navController
                            )
                        }
                    }
                    .addOnFailureListener {e ->
                        postError(e.localizedMessage ?: "Error uploading photo Firebase Storage")
                    }
            }
        }
    }

    private fun updateProfilePhotoUrlInAllCollections(
        documentId: String,
        newProfilePhotoUrl: String,
        navController: NavController
    ) {
        val db = FirebaseFirestore.getInstance()

        val collectionsToUpdate = listOf("artist", "event_founder", "user")

        collectionsToUpdate.forEach { collectionName ->
            val documentReference = db.collection(collectionName).document(documentId)

            documentReference.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    documentReference.update("profilePhotoURL", newProfilePhotoUrl)
                        .addOnSuccessListener {
                            Log.d(
                                "FirestoreUpdate",
                                "Document in $collectionName updated successfully"
                            )
                            navController.navigate(Constants.NAVIGATION_HOME_PAGE)
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "FirestoreUpdate",
                                "Error updating document in $collectionName",
                                e
                            )
                            postError(e.localizedMessage ?: "Error updating document in $collectionName")
                        }
                } else {
                    Log.d(
                        "FirestoreUpdate",
                        "Document with ID $documentId not found in $collectionName"
                    )
                    postError("Document with ID $documentId not found in $collectionName")
                }
            }.addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "Error getting document from $collectionName", e)
                postError(e.localizedMessage ?: "Error getting document.")
            }
        }
    }
    private fun postError(message: String) {
        (errorMessages as MutableLiveData).postValue(message)
    }

    fun clearError() {
        (errorMessages as MutableLiveData).postValue("")
    }

}