package edu.uark.ahnelson.openstreetmap2024.NewPinActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.uark.ahnelson.openstreetmap2024.Repository.Pin
import edu.uark.ahnelson.openstreetmap2024.Repository.PinRepository
import kotlinx.coroutines.launch

class NewPinViewModel(private val repository: PinRepository) : ViewModel() {
    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    val _pin = MutableLiveData<Pin>().apply{value=null}
    val _pinTemp = MutableLiveData<Pin>().apply{value=null}
    val pin: LiveData<Pin>
        get() = _pin
    val pinTemp: LiveData<Pin>
        get() = _pin

    fun start(pinId:Int){
        viewModelScope.launch {
            repository.allPins.collect{
                _pin.value = it[pinId]
            }
        }
    }

    fun insert(pin: Pin) = viewModelScope.launch {
        repository.insert(pin)
    }

    fun update(pin: Pin) = viewModelScope.launch {
        repository.update(pin)
    }

    fun delete(pinId: Int) = viewModelScope.launch {
        repository.delete(pinId)
    }

    fun getTempId(pinId: Int){
        viewModelScope.launch {
            repository.allPins.collect{ pins ->
                _pinTemp.value = pins.values.find { it.tempID == pinId }
            }
        }
    }
}

class NewPinViewModelFactory(private val repository: PinRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewPinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewPinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
