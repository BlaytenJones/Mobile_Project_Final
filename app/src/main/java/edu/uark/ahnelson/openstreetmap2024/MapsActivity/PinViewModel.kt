package edu.uark.ahnelson.openstreetmap2024.MapsActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import edu.uark.ahnelson.openstreetmap2024.Repository.Pin
import edu.uark.ahnelson.openstreetmap2024.Repository.PinRepository
import kotlinx.coroutines.launch

class PinViewModel(private val repository: PinRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPins: LiveData<Map<Int,Pin>> = repository.allPins.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(task: Pin) = viewModelScope.launch {
        repository.insert(task)
    }
}

class PinViewModelFactory(private val repository: PinRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
