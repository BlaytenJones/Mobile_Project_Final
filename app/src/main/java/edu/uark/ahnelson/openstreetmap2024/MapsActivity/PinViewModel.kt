package edu.uark.ahnelson.openstreetmap2024.MapsActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import edu.uark.ahnelson.openstreetmap2024.Repository.Pin
import edu.uark.ahnelson.openstreetmap2024.Repository.JSONPlaceholderRepository
import kotlinx.coroutines.launch

class PinViewModel(private val repository: JSONPlaceholderRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPins: LiveData<Map<Int, Pin>> = repository.allPins.asLiveData()
    val db = Firebase.firestore

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(pin: Pin) = viewModelScope.launch {
        repository.insertPinIntoRemoteDatasource(pin)
    }

    fun insertU(email: String, uid: String) = viewModelScope.launch {
        repository.insertUserIntoRemoteDatabase(email, uid)
    }

    fun getUserFromRemoteDatasource(email: String, onResult: (Int) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Extract the user ID
                    val document = querySnapshot.documents[0]
                    val userId = document.getLong("uid")?.toInt() ?: -1
                    Log.d("FirebaseGet", "Found UID: $userId")
                    onResult(userId) // Pass the result to the callback
                } else {
                    Log.d("FirebaseGet", "No user found with the provided email")
                    onResult(-1) // Pass a default value for "not found"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseGet", "Error retrieving user by email", exception)
                onResult(-1) // Pass a default error value
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
