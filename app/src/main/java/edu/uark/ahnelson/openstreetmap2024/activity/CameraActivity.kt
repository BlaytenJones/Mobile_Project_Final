package edu.uark.ahnelson.openstreetmap2024.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.openstreetmap2024.viewmodel.NewPinViewModel
import edu.uark.ahnelson.openstreetmap2024.viewmodel.NewPinViewModelFactory
import java.io.File
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.data.entity.Pin
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CameraActivity() : AppCompatActivity() {

    companion object {
        var refreshCallback: (() -> Unit)? = null
        fun registerRefreshCallback(callback: () -> Unit) {
            refreshCallback = callback
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshCallback = null // Prevent memory leaks
    }

    var pinsDate = ""
    private lateinit var editDesc: EditText

    // Example method to trigger the refresh
    fun triggerRefresh() {
        refreshCallback?.invoke()
    }

    val newPinViewModel: NewPinViewModel by viewModels {
        NewPinViewModelFactory((application as PinsApplication).repository)
    }
    var newInst = false
    var lon: Double = 0.0
    var lat: Double = 0.0
    var photoAdded = false
    var QR = ""
    var uid = ""
    var currUid = ""
    var desc = ""
    var id = -1

    var currentPhotoPath = ""
    lateinit var imageView: ImageView
    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED){
            Log.d("MainActivity","Picture Intent Cancelled")
        }else{
            setPic()
            Log.d("MainActivity","Picture Successfully taken at $currentPhotoPath")
        }
    }

    private fun formatDate(datetime: Date, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(datetime)
    }

    private fun galleryAddPic(filename:String){
        //Make sure to call this function on a worker thread, else it will block main thread
        var fos: OutputStream?
        var imageUri: Uri?
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        //use application context to get contentResolver
        val contentResolver = application.contentResolver

        contentResolver.also { resolver ->
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath)

        fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }

        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        imageUri?.let { contentResolver.update(it, contentValues, null, null) }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.camera_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cameraActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        imageView = findViewById(R.id.imageView)
        val fab = findViewById<FloatingActionButton>(R.id.fabTakePicture)
        val timestamp = findViewById<TextView>(R.id.timestamp)
        newInst = intent.getBooleanExtra("NEW",false)
        lat = intent.getDoubleExtra("LAT", 0.0)
        lon = intent.getDoubleExtra("LON",0.0)
        id = intent.getIntExtra("ID",-1)
        currUid = intent.getStringExtra("CURR_UID").toString()
        uid = intent.getStringExtra("UID").toString()
        editDesc = findViewById(R.id.edit_desc)
        findViewById<FloatingActionButton>(R.id.submit).setOnClickListener {
            savePicture()
        }
        //if this is a new pin instance...
        if(newInst){
            editDesc.isEnabled = true
            takeAPicture()
            //let the user take retake a picture
            fab.setOnClickListener {
                takeAPicture()
            }
        }else{
            desc = intent.getStringExtra("DESC").toString()
            editDesc.setText(desc)
            //else just look at it
            if(uid == currUid){
                //allow editing if it is the user's post
                editDesc.isEnabled = true
                Log.d("TESSTUID", "UID: $uid, currUID: $currUid")
            }else{
                Log.d("TESSTUID", "UID: $uid, currUID: $currUid")
            }
            QR = intent.getStringExtra("QR").toString()
            currentPhotoPath = intent.getStringExtra("FILEPATH").toString()
            setPic()
            fab.visibility = View.GONE
            timestamp.text = intent.getStringExtra("DATE").toString()
        }
    }

    private fun createFilePath(): String {
        // Create an image file name
        val currTime = Date()
        pinsDate = formatDate(currTime, "MM/dd/yyyy HH:mm:ss")
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(currTime)
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intent
        return image.absolutePath
    }

    private fun takeAPicture(){
        val pictureIntent: Intent = Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if(pictureIntent.resolveActivity(packageManager)!=null){
            val filepath = createFilePath()
            val myFile = File(filepath)
            currentPhotoPath = filepath
            val photoUri = FileProvider.getUriForFile(this,"edu.uark.ahnelson.OpenStreetMap2024.fileprovider",myFile)
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            takePictureResultLauncher.launch(pictureIntent)
        }
    }

    private fun setPic() {
        photoAdded = true
        val targetW: Double = imageView.width /1.2

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight/2
        val photoRatio:Double = (photoH.toDouble())/(photoW.toDouble())
        val targetH: Double = (targetW * photoRatio)
        // Determine how much to scale down the image
        val scaleFactor = Math.max(1, Math.min((photoW / targetW).roundToInt(), (photoH / targetH).roundToInt()))


        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        imageView.setImageBitmap(bitmap)
        imageView.invalidate()
        Log.d("FATAL","Picture set to $currentPhotoPath")
    }

    private fun savePicture(){
        if(photoAdded) {
            galleryAddPic(currentPhotoPath)
            if(uid == currUid){
                if(newInst){
                    newPinViewModel.insert(
                        Pin(id, currentPhotoPath, editDesc.text.toString(), pinsDate, lat, lon, QR, null, uid)
                    )
                }else{
                    newPinViewModel.update(
                        Pin(id, currentPhotoPath, editDesc.text.toString(), pinsDate, lat, lon, QR, null, uid)
                    )
                }
            }
            triggerRefresh()
            finish()
        }
    }
}
