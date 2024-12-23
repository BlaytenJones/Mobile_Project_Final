package edu.uark.ahnelson.openstreetmap2024.util


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority


//Interface for locationUtilCallbacks
interface LocationUtilCallback {
    //If unsuccessful due to permissions, we need to request them
    fun requestPermissionCallback()
    //If successful and we obtain a location object, return that through a parameter
    fun locationUpdatedCallback(location: Location)
}

/**
 * Returns a [LocationCallback] object with a dynamically created
 * onLocationResult callback function. Takes a [LocationUtilCallback]
 * as a parameter to pass locationResult Objects through a single dynamically
 * defined callback object
 */
fun createLocationCallback(callback: LocationUtilCallback): LocationCallback {
    val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            //if locationResult.lastLocation isn't null
            locationResult.lastLocation?.let {
                //Send the location out through the [callback] object
                callback.locationUpdatedCallback(it)
                //And log to make sure we don't keep doing it when we aren't supposed to
                Log.d("LocationUtil","Current Location is [Lat: ${it.latitude}, Long: ${it.longitude}]")
            }
        }
    }
    return locationCallback
}

/**
 * creates a recurring locationRequest
 * first check for permissions
 * then request updates
 *
 * Couple points -- This is not modular yet. Probably should take a locationRequest
 * object as a parameter, and probably a thread as a parameter
 */
fun createLocationRequest(context: Context, locationProviderClient: FusedLocationProviderClient, locationCallback: LocationCallback):Boolean{
    val locationRequest: LocationRequest = LocationRequest.Builder(1000)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateIntervalMillis(1000).build()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }
    locationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    return true
}

