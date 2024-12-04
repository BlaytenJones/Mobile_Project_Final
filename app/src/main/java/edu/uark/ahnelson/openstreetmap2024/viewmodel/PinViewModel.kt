package edu.uark.ahnelson.openstreetmap2024.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import edu.uark.ahnelson.openstreetmap2024.data.entity.Pin
import edu.uark.ahnelson.openstreetmap2024.Repository.JSONPlaceholderRepository
import edu.uark.ahnelson.openstreetmap2024.data.entity.MintedToken
import edu.uark.ahnelson.openstreetmap2024.data.entity.User
import kotlinx.coroutines.launch
//import androidx.lifecycle.asLiveData

class PinViewModel(private val repository: JSONPlaceholderRepository) : ViewModel() {

    //val allPins: LiveData<Map<Int, Pin>> = repository.allPins.asLiveData()
    val db = Firebase.firestore


    // Launching a new coroutine to insert the data in a non-blocking way
    /*fun insert(pin: Pin) = viewModelScope.launch {
        repository.insertPinIntoRemoteDatasource(pin)
    }*/

    fun insertU(email: String, uid: String) = viewModelScope.launch {
        repository.insertUserIntoRemoteDatabase(email, uid)
    }

    private fun getUserFromRemoteDatasource(uid: String, onResult: (User?) -> Unit) {
        db.collection("users")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Extract the user document
                    val document = querySnapshot.documents[0]

                    // Convert the document to a User object using the Firestore data directly
                    val inventoryList = document.get("inventory") as? List<Map<String, Any>> ?: emptyList()
                    val solvedList = document.get("solved") as? List<Int> ?: emptyList()

                    val inventory = inventoryList.map { map ->
                        MintedToken(
                            tokenId = map["tokenId"] as? Int ?: 0,
                            mintNum = map["mintNum"] as? Int ?: 0
                        )
                    }

                    val user = User(
                        email = document.getString("email") ?: "",
                        uid = document.getString("uid") ?: "",
                        inventory = inventory,
                        solved = solvedList
                    )

                    Log.d("FirebaseGet", "Found User: $user; Inventory: $inventoryList")
                    onResult(user) // Pass the user object to the callback
                } else {
                    Log.d("FirebaseGet", "No user found with the provided UID")
                    onResult(null) // Pass null for "not found"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseGet", "Error retrieving user by UID", exception)
                onResult(null) // Pass null for error
            }
    }







    fun getPinsFromRemoteDatasource(onResult: (List<Pin>) -> Unit) {
        db.collection("pins")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Map each document to a Pin object
                    val pins = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Pin::class.java) // Convert to Pin object
                    }
                    Log.d("FirebaseGet", "Found ${pins.size} pins")
                    onResult(pins) // Pass the list of pins to the callback
                } else {
                    Log.d("FirebaseGet", "No pins found")
                    onResult(emptyList()) // Return an empty list if no pins are found
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseGet", "Error retrieving pins", exception)
                onResult(emptyList()) // Return an empty list in case of an error
            }
    }

    fun fetchInventory(userId: String, onResult: (List<MintedToken>) -> Unit) {
        db.collection("users")
            .whereEqualTo("uid", userId) // Find document where 'uid' matches userId
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents.first() // Get the first document (should only be one)
                    val inventory = document.get("inventory") as? List<Map<String, Any>> // Assuming inventory is a list

                    val tokens = inventory?.map { tokenData ->
                        MintedToken(
                            tokenId = (tokenData["tokenId"] as? Long)?.toInt() ?: 0,
                            mintNum = (tokenData["mintNum"] as? Long)?.toInt() ?: 0
                        )
                    } ?: emptyList() // Return empty list if no inventory

                    onResult(tokens)
                } else {
                    onResult(emptyList()) // No matching document found
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    fun addTokenToInventory(uid: String, mintedToken: MintedToken) {
        // Launch a coroutine to call the suspend function
        viewModelScope.launch {
            // Use the callback-based method to get the user
            getUserFromRemoteDatasource(uid) { user ->
                if (user != null) {
                    Log.d("MainActivity", "User retrieved: ${user.email}")
                    // Now you can call the suspend function to add the token
                    viewModelScope.launch {  // Launch another coroutine to call the suspend function
                        repository.addTokenToUser(user, mintedToken)
                    }
                } else {
                    Log.d("MainActivity", "User not found or error occurred")
                }
            }
        }
    }

}

class PinViewModelFactory(private val repository: JSONPlaceholderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
