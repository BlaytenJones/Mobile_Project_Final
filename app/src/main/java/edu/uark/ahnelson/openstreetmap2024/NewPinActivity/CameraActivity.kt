package edu.uark.ahnelson.openstreetmap2024.NewPinActivity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
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
import androidx.room.ColumnInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uark.ahnelson.openstreetmap2024.MapsActivity.PinViewModel
import edu.uark.ahnelson.openstreetmap2024.MapsActivity.PinViewModelFactory
import edu.uark.ahnelson.openstreetmap2024.PinsApplication
import java.io.File
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.Repository.Pin
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CameraActivity() : AppCompatActivity() {
    var pinsDate = ""
    private lateinit var editDesc: EditText

    private lateinit var pin: Pin
    val newPinViewModel: NewPinViewModel by viewModels {
        NewPinViewModelFactory((application as PinsApplication).repository)
    }
    var newInst = false
    var tempId = -1
    var lon: Double = 0.0
    var lat: Double = 0.0
    var mainId = 0
    var photoAdded = false

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
        var fos: OutputStream? = null
        var imageUri: Uri? = null
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
        mainId = intent.getIntExtra("EXTRA_ID",-1)
        tempId = intent.getIntExtra("TEMP_ID",-1)
        newInst = intent.getBooleanExtra("NEW",false)
        lat = intent.getDoubleExtra("LAT", 0.0)
        lon = intent.getDoubleExtra("LON",0.0)
        editDesc = findViewById(R.id.edit_desc)
        findViewById<FloatingActionButton>(R.id.submit).setOnClickListener {
            savePicture()
        }
        //if this is a new pin instance...
        if(newInst){
            takeAPicture()
            //let the user take retake a picture
            fab.setOnClickListener {
                takeAPicture()
            }
        }else{
            //otherwise, do not let the user change the picture
            fab.visibility = View.INVISIBLE
            if(tempId!=-1){
                //The task was made during the lifetime of the activity
                newPinViewModel.getTempId(tempId)
            }else{
                //The task is from the database
                newPinViewModel.start(mainId)
            }
            newPinViewModel.pin.observe(this){
                if(it != null){
                    if(tempId!=-1) mainId = it.id!! //get main ID if it is made in the lifetime
                    pinsDate = it.date
                    editDesc.setText(it.desc)
                    timestamp.text = pinsDate
                    currentPhotoPath = it.filepath
                    setPic()
                }
            }
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
            val myFile: File = File(filepath)
            currentPhotoPath = filepath
            val photoUri = FileProvider.getUriForFile(this,"edu.uark.ahnelson.OpenStreetMap2024.fileprovider",myFile)
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            takePictureResultLauncher.launch(pictureIntent)
        }
    }

    private fun setPic() {
        photoAdded = true
        val targetW: Double = imageView.getWidth()/1.2

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
            if(newInst){
                newPinViewModel.insert(
                    Pin(null, currentPhotoPath, editDesc.text.toString(), pinsDate, lat, lon, tempId)
                )
            }else{
                newPinViewModel.update(
                    Pin(mainId, currentPhotoPath, editDesc.text.toString(), pinsDate, lat, lon, -1)
                )
            }
            finish()
        }
    }
}
