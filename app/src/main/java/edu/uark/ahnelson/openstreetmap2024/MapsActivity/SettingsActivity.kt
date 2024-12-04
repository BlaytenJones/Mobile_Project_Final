package edu.uark.ahnelson.openstreetmap2024.MapsActivity

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.uark.ahnelson.openstreetmap2024.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.mainLayout)

        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(1500)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        auth = Firebase.auth

        val user = auth.currentUser
        val userEmail = user?.email ?: "No email found"
        val emailTextView: TextView = findViewById(R.id.email)
        emailTextView.text = userEmail

        findViewById<ImageButton>(R.id.logoutButton).setOnClickListener {
            auth.signOut() // Sign out from Firebase
            Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_SHORT).show()

            // Redirect user to SignInActivity or any other screen
            val signInIntent = Intent(this, SignInActivity::class.java)
            signInIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(signInIntent)
            finish()
        }
    }
}