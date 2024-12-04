package edu.uark.ahnelson.openstreetmap2024.activity.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import edu.uark.ahnelson.openstreetmap2024.activity.PinsApplication
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.Util.LocationUtilCallback
import edu.uark.ahnelson.openstreetmap2024.Util.createLocationCallback
import edu.uark.ahnelson.openstreetmap2024.Util.createLocationRequest
import edu.uark.ahnelson.openstreetmap2024.Util.replaceFragmentInActivity
import edu.uark.ahnelson.openstreetmap2024.activity.map.OpenStreetMapFragment
import edu.uark.ahnelson.openstreetmap2024.viewmodel.PinViewModel
import edu.uark.ahnelson.openstreetmap2024.viewmodel.PinViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

class MapsActivity : AppCompatActivity() {
    //mapsFragment object to hold the fragment
    private lateinit var mapsFragment: OpenStreetMapFragment
    //Boolean to keep track of whether permissions have been granted
    private var locationPermissionEnabled: Boolean = false
    //Boolean to keep track of whether activity is currently requesting location Updates
    private var locationRequestsEnabled: Boolean = false
    //Member object for the FusedLocationProvider
    private lateinit var locationProviderClient: FusedLocationProviderClient
    //Member object for the last known location
    private lateinit var mCurrentLocation: Location
    //Member object to hold onto locationCallback object
    //Needed to remove requests for location updates
    private lateinit var mLocationCallback: LocationCallback

    //ViewModel object to communicate between Activity and repository
    private val pinViewModel: PinViewModel by viewModels {
        PinViewModelFactory((application as PinsApplication).repository)
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            //If successful, startLocationRequests
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }
            //If successful at coarse detail, we still want those
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }

            else -> {
                //Otherwise, send toast saying location is not enabled
                locationPermissionEnabled = false
                Toast.makeText(this, "Location Not Enabled", Toast.LENGTH_LONG)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Configuration.getInstance().load(this, getSharedPreferences(
            "${packageName}_preferences", Context.MODE_PRIVATE))

        val userID = intent.getStringExtra("USER_ID").toString()
        Log.d("UIDTEST", "User ID: $userID")

        mapsFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as OpenStreetMapFragment? ?: OpenStreetMapFragment.newInstance().also {
            replaceFragmentInActivity(it, R.id.fragmentContainerView)
        }

        mapsFragment.setUserId(userID)
        mapsFragment.setRefreshCallback(::refreshPins)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        checkForLocationPermission()


        pinViewModel.getPinsFromRemoteDatasource() { retrievedPins ->
            if (retrievedPins.isNotEmpty()) {
                retrievedPins.forEach { pin ->
                    mapsFragment.addMarker(pin)
                }
            } else {
                Log.d("PinData", "No pins retrieved")
            }
        }

    }

    private fun checkForLocationPermission(){
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationRequests()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    //LocationUtilCallback object
    //Dynamically defining two results from locationUtils
    //Namely requestPermissions and locationUpdated
    private val locationUtilCallback = object : LocationUtilCallback {
        //If locationUtil request fails because of permission issues
        //Ask for permissions
        override fun requestPermissionCallback() {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        //If locationUtil returns a Location object
        //Populate the current location and log
        override fun locationUpdatedCallback(location: Location) {
            mCurrentLocation = location
            mapsFragment.changeCenterLocation(GeoPoint(location.latitude,location.longitude))
            Log.d(
                "MainActivity",
                "Location is [Lat: ${location.latitude}, Long: ${location.longitude}]"
            )
        }
    }

    private fun startLocationRequests() {
        //If we aren't currently getting location updates
        if (!locationRequestsEnabled) {
            //create a location callback
            mLocationCallback = createLocationCallback(locationUtilCallback)
            //and request location updates, setting the boolean equal to whether this was successful
            locationRequestsEnabled =
                createLocationRequest(this, locationProviderClient, mLocationCallback)
        }
    }

     fun refreshPins() {
        pinViewModel.getPinsFromRemoteDatasource { retrievedPins ->
            if (retrievedPins.isNotEmpty()) {
                mapsFragment.clearMarkers() // Clear existing markers if needed
                retrievedPins.forEach { pin ->
                    mapsFragment.addMarker(pin)
                }
            } else {
                Log.d("PinData", "No pins retrieved")
            }
        }
    }
}