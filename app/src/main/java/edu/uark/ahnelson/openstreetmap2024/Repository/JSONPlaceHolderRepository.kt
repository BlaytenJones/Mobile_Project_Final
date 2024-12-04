package edu.uark.ahnelson.openstreetmap2024.Repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import androidx.activity.viewModels
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JSONPlaceholderRepository(private val jsonPlaceHolderDao: JSONPlaceHolderDao) {

    val allPins: Flow<Map<Int, Pin>> = jsonPlaceHolderDao.getOrderedPins()
    val db = Firebase.firestore

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updatePinInRoomDatabase(pin:Pin){
        jsonPlaceHolderDao.update(pin)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePinFromRoomDatabase(localId:Int){
        jsonPlaceHolderDao.delete(localId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertPinIntoRemoteDatasource(pin:Pin){
        db.collection("pins")
            .add(pin)
            .addOnSuccessListener { documentReference ->
                Log.d("FirebaseAdd", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseAdd", "Error adding document", e)
            }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getUserFromWebserviceById(email: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Get the first matching document
                    val document = querySnapshot.documents[0]
                    val uid = document.getLong("currUID")?.toInt()
                    Log.d("FirebaseGet", "Found UID: $uid")
                } else {
                    Log.d("FirebaseGet", "No user found with the provided email")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseGet", "Error retrieving user by email", exception)
            }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertUserIntoRemoteDatabase(email:String, currUID:String){
        db.collection("users")
            .add(User(email, currUID, emptyList(), emptyList()))
            .addOnSuccessListener { documentReference ->
                Log.d("FirebaseAdd", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseAdd", "Error adding document", e)
            }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updatePinInRemoteDatasource(pin:Pin){
        val usersRef = db.collection("pins")

        // Query for the pin based on localId and uid
        Log.d("UPDATE", pin.uid + " " + pin.localId.toString())
        val query = usersRef
            .whereEqualTo("localId", pin.localId)
            .whereEqualTo("uid", pin.uid)

        try {
            query.get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        // Update the document with the new pin details
                        val docRef = usersRef.document(document.id)
                        docRef.update(
                            mapOf(
                                "filepath" to pin.filepath,
                                "desc" to pin.desc,
                                "date" to pin.date,
                                "lat" to pin.lat,
                                "lon" to pin.lon,
                                "QRCode" to pin.QRCode,
                                "id" to pin.id,
                            )
                        ).addOnSuccessListener {
                            Log.d("UPDATE", "Pin updated successfully!")
                        }.addOnFailureListener { e ->
                            Log.e("UPDATE", "Error updating pin: $e")
                        }
                    }
                } else {
                    Log.d("UPDATE", "No pin found with matching pinId and uid.")
                }
            }.addOnFailureListener { e ->
                Log.e("UPDATE", "Error fetching documents: $e")
            }
        } catch (e: Exception) {
            Log.e("UPDATE", "Exception occurred: $e")
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addTokenToUser(user: User, mintedToken: MintedToken) {
        val usersRef = db.collection("users")

        Log.d("UPDATE_TIME", "Attempting to update user: ${user.uid} with tokenId: ${mintedToken.tokenId} and mintNum: ${mintedToken.mintNum}")

        val query = usersRef.whereEqualTo("uid", user.uid)

        // Create a Map representation of the MintedToken
        val tokenMap = mapOf(
            "tokenId" to mintedToken.tokenId,
            "mintNum" to mintedToken.mintNum
        )

        try {
            val documents = query.get().await()
            if (!documents.isEmpty) {
                for (document in documents) {
                    val docRef = usersRef.document(document.id)
                    Log.d("UPDATE_TIME", "Found document with ID: ${document.id}")

                    // Add the token to the inventory as a map
                    docRef.update("inventory", FieldValue.arrayUnion(tokenMap)).await()

                    // Check the document data after update
                    val docSnapshot = docRef.get().await()
                    Log.d("UPDATE_TIME", "Updated document data: ${docSnapshot.data}")

                    Log.d("UPDATE_TIME", "User updated successfully!")
                }
            } else {
                Log.d("UPDATE_TIME", "No user found with matching uid.")
            }
        } catch (e: Exception) {
            Log.e("UPDATE_TIME", "Error occurred during Firestore update: $e")
        }
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePinFromRemoteDatasource(localId:Int, uid: String){
        val usersRef = db.collection("pins")
        val query = usersRef.whereEqualTo("localId", localId).whereEqualTo("uid", uid)

        // Get the matching document
        query.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // Get the first document (assuming there's only one match)
                    val document = snapshot.documents[0]

                    // Delete the document
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("DELETE", "Document successfully deleted!")
                        }
                        .addOnFailureListener { exception ->
                            Log.d("DELETE", "Error deleting document: ${exception.message}")
                        }
                } else {
                    Log.d("DELETE", "No matching document found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("DELETE", "Error getting document: ${exception.message}")
            }
    }
}
