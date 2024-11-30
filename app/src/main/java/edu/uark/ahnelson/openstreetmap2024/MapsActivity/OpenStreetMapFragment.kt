package edu.uark.ahnelson.openstreetmap2024.MapsActivity

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import edu.uark.ahnelson.openstreetmap2024.NewPinActivity.CameraActivity
import edu.uark.ahnelson.openstreetmap2024.PinsApplication
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.Repository.Pin
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay




class OpenStreetMapFragment : Fragment(), Marker.OnMarkerClickListener {

    private var refreshCallback: (() -> Unit)? = null

    // Method to set the callback
    fun setRefreshCallback(callback: () -> Unit) {
        refreshCallback = callback
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
    private var userId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt("USER_ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_open_street_map, container, false)
        mMap = root.findViewById(R.id.map)
        cameraButton = root.findViewById(R.id.imageButton)

        cameraButton.setOnClickListener {
            val intent = Intent(requireActivity(), CameraActivity::class.java)
            intent.putExtra("NEW",true)
            intent.putExtra("LAT", curLocation.latitude)
            intent.putExtra("LON", curLocation.longitude)
            intent.putExtra("UID", userId)
            intent.putExtra("CURR_UID", userId)
            CameraActivity.refreshCallback = ::triggerRefresh
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
        this.mLocationOverlay.enableMyLocation();
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
        mMap.getOverlays().add(copyrightOverlay)

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
        mapController.setCenter(curLocation);


    }

    fun addMarker(pin: Pin) {
        val geoPoint = GeoPoint(pin.lat, pin.lon)
        val startMarker = Marker(mMap)
        startMarker.position = geoPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.setOnMarkerClickListener(this)
        startMarker.id = id.toString()
        startMarker.relatedObject = pin
        startMarker.icon = ResourcesCompat.getDrawable(resources, R.drawable.map_pin_small, null)
        mMap.getOverlays().add(startMarker)
    }

    fun clearMarkers() {
        mMap.overlays.clear()
        setupMapOptions()
    }

    fun clearOneMarker(id: Int) {
        for (overlay in mMap.overlays) {
            if (overlay is Marker) {
                if (overlay.id == id.toString()) {
                    mMap.overlays.remove(overlay)
                }
            }
        }
    }

    override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
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
        CameraActivity.refreshCallback = ::triggerRefresh
        startActivity(intent)
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment OpenStreetMapFragment.
         */
        @JvmStatic
        fun newInstance(userID: Int) =
            OpenStreetMapFragment().apply {
                val fragment = OpenStreetMapFragment()
                val args = Bundle().apply {
                    putInt("USER_ID", userID)
                }
                fragment.arguments = args
                return fragment
            }
    }
}