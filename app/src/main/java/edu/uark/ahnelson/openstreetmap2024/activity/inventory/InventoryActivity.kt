package edu.uark.ahnelson.openstreetmap2024.activity.inventory

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.uark.ahnelson.openstreetmap2024.activity.PinsApplication
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.data.entity.MintedToken
import edu.uark.ahnelson.openstreetmap2024.viewmodel.PinViewModel
import edu.uark.ahnelson.openstreetmap2024.viewmodel.PinViewModelFactory

class InventoryActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val userViewModel: PinViewModel by viewModels {
        PinViewModelFactory((application as PinsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.inventory_activity)

        val constraintLayout = findViewById<ConstraintLayout>(R.id.mainLayout)

        val animationDrawable = constraintLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(1500)
        animationDrawable.setExitFadeDuration(3000)
        animationDrawable.start()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventory)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 tokens per row
        auth = Firebase.auth
        auth.currentUser

        /*findViewById<Button>(R.id.button).setOnClickListener {
            generateToken()
        }*/

        auth.currentUser?.uid?.let {
            userViewModel.fetchInventory(it) { tokens ->
                val adapter = InventoryAdapter(tokens)
                recyclerView.adapter = adapter
            }
        }
    }

    /*private fun generateToken() {
        userViewModel.addTokenToInventory(auth.currentUser!!.uid, MintedToken((1..5).random(), (1..500).random()))
    }*/

}