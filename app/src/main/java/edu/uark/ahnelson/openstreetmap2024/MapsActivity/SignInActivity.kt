package edu.uark.ahnelson.openstreetmap2024.MapsActivity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.Repository.User
import edu.uark.ahnelson.openstreetmap2024.PinsApplication

class SignInActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private var showOneTapUI = true

    private lateinit var editUser: EditText
    private lateinit var editPass: EditText

    private val loginResultHandler = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        // handle intent result here
        if(result.resultCode == RESULT_OK){
            Log.d(TAG, "RESULT_OK.")
        }
        if(result.resultCode == RESULT_CANCELED){
            Log.d(TAG, "RESULT_CANCELED.")
        }
        if (result.resultCode == RESULT_FIRST_USER){
            Log.d(TAG, "RESULT_FIRST_USER.")
        }
        try {
            val credential =
                oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            val password = credential.password
            if (idToken != null) {
                // Got an ID token from Google. Use it to authenticate
                // with your backend.
                Log.d(TAG, "Got ID token.")
            } else if (password != null) {
                // Got a saved username and password. Use them to authenticate
                // with your backend.
                Log.d(TAG, "Got password.")
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Log.d(TAG, "One-tap dialog was closed.")
                    // Don't re-prompt the user.
                    showOneTapUI = false
                }
                CommonStatusCodes.NETWORK_ERROR ->
                    Log.d(TAG, "One-tap encountered a network error.")
                else ->
                    Log.d(TAG, "Couldn't get credential from result."
                            + e.localizedMessage)
            }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPrefs"
    private val CURR_UID = "currUID"
    var currUID = 0

    private val userViewModel: PinViewModel by viewModels {
        PinViewModelFactory((application as PinsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_in)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.mainLayout)

        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(1500)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        auth = Firebase.auth
        editUser = findViewById(R.id.editTextTextEmailAddress)
        editPass = findViewById(R.id.editTextTextPassword)
        findViewById<Button>(R.id.btnCreateUser).setOnClickListener {
            createUserWithUsernamePassword()
        }
        findViewById<Button>(R.id.btnLogInWithGoogle).setOnClickListener{
            logInWithUsernamePassword()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

    }

    fun createUserWithUsernamePassword(){
        val email = editUser.text.toString()
        val password = editPass.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    //StartActivity
                    //finish()
                    Toast.makeText(this,"User signed in!",Toast.LENGTH_LONG).show()
                    Log.d(TAG,"User UUID:${auth.currentUser.toString()}")
                    sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    currUID = sharedPreferences.getInt(CURR_UID, 0) + 1
                    userViewModel.insertU(email, currUID, this)
                    with(sharedPreferences.edit()) {
                        putInt(CURR_UID, currUID)
                        apply() // Apply the changes asynchronously
                    }
                    val launchSecondActivityIntent = Intent(this,MapsActivity::class.java)
                    launchSecondActivityIntent.putExtra("USER_ID", currUID)
                    startActivity(launchSecondActivityIntent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun logInWithUsernamePassword(){
        val email = editUser.text.toString()
        val password = editPass.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(this,"User signed in!",Toast.LENGTH_LONG).show()
                    Log.d(TAG,"User UUID:${auth.currentUser.toString()}")
                    val launchSecondActivityIntent = Intent(this,MapsActivity::class.java)
                    launchSecondActivityIntent.putExtra("USER_EMAIL", email)
                    startActivity(launchSecondActivityIntent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}