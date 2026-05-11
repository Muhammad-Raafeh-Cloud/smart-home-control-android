package com.example.projectdeliverable1.data

import com.example.projectdeliverable1.models.Device
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Assignment #04 - Firebase Firestore Helper
 *
 * Handles:
 *  - Real-time sync of devices to Firestore (F2)
 *  - Two Firestore collections: "users" and "devices" (logical relationship via userId)
 *  - Listening for live updates on the devices collection
 *  - CRUD operations on Firestore (add, update, delete)
 */
object FirestoreHelper {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Collection names
    private const val COL_USERS = "users"
    private const val COL_DEVICES = "devices"

    // -------------------------------------------------------------------------
    // F2 - Real-time sync: listen for changes in the user's devices collection
    // -------------------------------------------------------------------------

    /**
     * F2 - syncUserData()
     * Attaches a real-time Firestore listener on the 'devices' sub-collection of the given user.
     * Any change made on another device is immediately pushed to this listener.
     *
     * @param userId      The Firebase Auth UID of the logged-in user.
     * @param onUpdate    Callback invoked with the updated list of devices whenever Firestore changes.
     * @param onError     Callback invoked if the listener encounters an error.
     * @return            A ListenerRegistration — call .remove() when you no longer need updates
     *                    (e.g., in onStop() or onDestroyView() of your Fragment).
     *
     * Firestore path:  users/{userId}/devices/{deviceId}
     */
    fun syncUserData(
        userId: String,
        onUpdate: (List<Device>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection(COL_USERS)
            .document(userId)
            .collection(COL_DEVICES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val devices = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DeviceFirestore::class.java)?.toDevice(doc.id)
                    }
                    onUpdate(devices)
                }
            }
    }

    // -------------------------------------------------------------------------
    // F2 - Write user profile to the top-level 'users' collection
    // -------------------------------------------------------------------------

    /**
     * Saves or updates a user profile document in the 'users' collection.
     * Called after login so we have a record of who is using the app.
     *
     * Firestore path:  users/{userId}
     */
    suspend fun saveUserProfile(userId: String, email: String, displayName: String): Result<Unit> {
        return try {
            val userMap = mapOf(
                "uid" to userId,
                "email" to email,
                "displayName" to displayName,
                "lastSeen" to System.currentTimeMillis()
            )
            db.collection(COL_USERS).document(userId).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // F2 - CRUD on the devices sub-collection
    // -------------------------------------------------------------------------

    /**
     * Adds a new device to Firestore under the logged-in user.
     * Firestore path:  users/{userId}/devices/{auto-id}
     */
    suspend fun addDevice(userId: String, device: Device): Result<String> {
        return try {
            val ref = db.collection(COL_USERS)
                .document(userId)
                .collection(COL_DEVICES)
                .add(device.toMap())
                .await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing device in Firestore.
     * Requires the Firestore document ID (firestoreId), not the SQLite integer ID.
     */
    suspend fun updateDevice(userId: String, firestoreId: String, device: Device): Result<Unit> {
        return try {
            db.collection(COL_USERS)
                .document(userId)
                .collection(COL_DEVICES)
                .document(firestoreId)
                .set(device.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a device document from Firestore.
     */
    suspend fun deleteDevice(userId: String, firestoreId: String): Result<Unit> {
        return try {
            db.collection(COL_USERS)
                .document(userId)
                .collection(COL_DEVICES)
                .document(firestoreId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Internal helper: Firestore data class and mapping functions
    // -------------------------------------------------------------------------

    /**
     * Firestore-compatible data class.
     * Firestore needs a no-arg constructor, so all fields have defaults.
     */
    data class DeviceFirestore(
        val name: String = "",
        val location: String = "",
        val status: String = "",
        val type: String = "",
        val lastUpdated: String = "",
        val description: String = ""
    ) {
        /** Convert Firestore document → your existing Device model */
        fun toDevice(firestoreDocId: String): Device {
            return Device(
                id = firestoreDocId.hashCode(),
                name = name,
                location = location,
                status = status,
                type = type,
                lastUpdated = lastUpdated,
                description = description,
                firestoreId = firestoreDocId
            )
        }
    }

    /** Convert your existing Device model → a Map for Firestore storage */
    private fun Device.toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "location" to location,
        "status" to status,
        "type" to type,
        "lastUpdated" to lastUpdated,
        "description" to description
    )
}
