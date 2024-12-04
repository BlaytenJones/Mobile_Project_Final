package edu.uark.ahnelson.openstreetmap2024.activity.map

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.uark.ahnelson.openstreetmap2024.activity.CameraActivity
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.activity.PinsApplication
import edu.uark.ahnelson.openstreetmap2024.data.entity.MintedToken
import edu.uark.ahnelson.openstreetmap2024.data.entity.Pin
import edu.uark.ahnelson.openstreetmap2024.viewmodel.PinViewModel
import edu.uark.ahnelson.openstreetmap2024.viewmodel.PinViewModelFactory
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay




class OpenStreetMapFragment : Fragment(), Marker.OnMarkerClickListener {

    private var refreshCallback: (() -> Unit)? = null
    private var userId: String = ""

    // Method to set the callback
    fun setRefreshCallback(callback: () -> Unit) {
        refreshCallback = callback
    }

    fun setUserId(userID: String) {
        Log.d("UIDTEST", "Creating new instance with userID: $userID")
        this.userId = userID
    }

    // Example method to trigger the refresh
    fun triggerRefresh() {
        refreshCallback?.invoke()
    }

    private lateinit var mMap: MapView
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var mCompassOverlay: CompassOverlay
    private lateinit var cameraButton: AppCompatImageButton
    private var curLocation = GeoPoint(34.74, -92.28)
    private lateinit var userViewModel: PinViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_open_street_map, container, false)
        mMap = root.findViewById(R.id.map)
        cameraButton = root.findViewById(R.id.imageButton)

        userViewModel = ViewModelProvider(this, PinViewModelFactory((requireActivity().application as PinsApplication).repository)).get(PinViewModel::class.java)

        auth = Firebase.auth
        auth.currentUser

        cameraButton.setOnClickListener {
            val intent = Intent(requireActivity(), CameraActivity::class.java)
            intent.putExtra("NEW",true)
            intent.putExtra("LAT", curLocation.latitude)
            intent.putExtra("LON", curLocation.longitude)
            intent.putExtra("UID", userId)
            intent.putExtra("CURR_UID", userId)
            CameraActivity.registerRefreshCallback(::triggerRefresh)
            startActivity(intent)
        }

        setupMapOptions()
        val mapController = mMap.controller
        mapController.setZoom(3.1)
        changeCenterLocation(curLocation)
        return root
    }

    override fun onResume() {
        super.onResume()
        mMap.onResume()
        refreshPins()
    }

    private fun refreshPins() {
        userViewModel.getPinsFromRemoteDatasource { retrievedPins ->
            if (retrievedPins.isNotEmpty()) {
                clearMarkers()
                retrievedPins.forEach { pin ->
                    addMarker(pin)
                }
            } else {
                Log.d("PinData", "No pins retrieved")
            }
        }
    }


    override fun onPause() {
        super.onPause()
        mMap.onPause()
    }

    private fun setupMapOptions() {
        mMap.isTilesScaledToDpi = true
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        addCopyrightOverlay()
        addLocationOverlay()
        addCompassOverlay()
        addMapScaleOverlay()
        addRotationOverlay()
    }

    private fun addRotationOverlay() {
        val rotationGestureOverlay = RotationGestureOverlay(mMap)
        rotationGestureOverlay.isEnabled
        mMap.setMultiTouchControls(true)
        mMap.overlays.add(rotationGestureOverlay)
    }

    private fun addLocationOverlay() {
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mMap)
        this.mLocationOverlay.enableMyLocation()
        mMap.overlays.add(mLocationOverlay)
    }

    private fun addCompassOverlay() {
        mCompassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mMap)
        mCompassOverlay.enableCompass()
        mMap.overlays.add(mCompassOverlay)
    }

    private fun addCopyrightOverlay() {
        val copyrightNotice: String =
            mMap.tileProvider.tileSource.copyrightNotice
        val copyrightOverlay = CopyrightOverlay(context)
        copyrightOverlay.setCopyrightNotice(copyrightNotice)
        mMap.overlays.add(copyrightOverlay)

    }

    private fun addMapScaleOverlay() {
        val dm: DisplayMetrics = context?.resources?.displayMetrics ?: return
        val scaleBarOverlay = ScaleBarOverlay(mMap)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        mMap.overlays.add(scaleBarOverlay)
    }

    fun changeCenterLocation(geoPoint: GeoPoint) {
        curLocation = geoPoint
        val mapController = mMap.controller
        mapController.setCenter(curLocation)


    }

    fun addMarker(pin: Pin) {
        val geoPoint = GeoPoint(pin.lat, pin.lon)
        val marker = Marker(mMap)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.map_pin_small, null)
        marker.relatedObject = pin

        marker.infoWindow = object : InfoWindow(R.layout.marker_popup, mMap) {
            override fun onOpen(item: Any?) {
                val view = mView
                val pinData = marker.relatedObject as? Pin ?: return
                view.findViewById<TextView>(R.id.marker_desc).text = pinData.desc

                // Set up Generate Token button
                val generateButton = view.findViewById<Button>(R.id.generate_token_button)
                generateButton.setOnClickListener {
                    generateToken(pin)
                    // Remove marker after generating the token
                    //mMap.overlays.remove(marker)
                    mMap.invalidate() // Refresh the map
                    close() // Close the info window
                }
            }

            override fun onClose() {
                // Hide the info window when dismissed
            }
        }

        marker.setOnMarkerClickListener { _, _ ->
            if (marker.isInfoWindowShown) {
                marker.closeInfoWindow() // Close if already open
            } else {
                marker.showInfoWindow() // Show the info window
            }
            true // Indicate the event was handled
        }

        mMap.overlays.add(marker)
    }


    private fun generateToken(pin: Pin) {
        // Add the token to the inventory
        userViewModel.addTokenToInventory(auth.currentUser!!.uid, MintedToken((1..5).random(), (1..500).random()))

        // Remove the pin associated with the generated token
        /*userViewModel.removePinFromDataSource(pin) { success ->
            if (success) {
                Log.d("PinViewModel", "Pin successfully removed!")
            } else {
                Log.d("PinViewModel", "Error removing pin")
            }
        }*/
    }

    fun clearMarkers() {
        mMap.overlays.clear()
        setupMapOptions()
    }

    /*fun clearOneMarker(id: Int) {
        for (overlay in mMap.overlays) {
            if (overlay is Marker) {
                if (overlay.id == id.toString()) {
                    mMap.overlays.remove(overlay)
                }
            }
        }
    }*/

    // Could use intents to provide more data in pins
    /*override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
        marker?.id?.let { Log.d("OpenStreetMapFragment", it) }
        val intent = Intent(requireActivity(), CameraActivity::class.java)
        //determine if the marker was made during the lifetime
        intent.putExtra("LAT",marker?.position?.latitude)
        intent.putExtra("LON",marker?.position?.longitude)
        intent.putExtra("NEW", false)
        val pin = marker?.relatedObject as? Pin
        if (pin != null) {
            // Add Pin object's attributes to the intent
            intent.putExtra("DESC", pin.desc)
            intent.putExtra("DATE", pin.date)
            intent.putExtra("QR_CODE", pin.QRCode)
            intent.putExtra("FILEPATH", pin.filepath)
            intent.putExtra("UID", pin.uid)
            intent.putExtra("CURR_UID", userId)
            intent.putExtra("ID", pin.id)
        } else {
            Log.d("OpenStreetMapFragment", "No related Pin object found for this marker")
        }
        CameraActivity.registerRefreshCallback(::triggerRefresh)
        startActivity(intent)
        return true
    }*/

    // marker click for generating tokens without QR or extra data
    override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
        marker?.showInfoWindow()
        return true // Return true to indicate the click was handled
    }

    companion object {
        @JvmStatic
        fun newInstance(): OpenStreetMapFragment {
            return OpenStreetMapFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}