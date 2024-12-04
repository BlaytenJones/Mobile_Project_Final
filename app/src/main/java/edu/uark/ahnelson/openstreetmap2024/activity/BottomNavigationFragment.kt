package edu.uark.ahnelson.openstreetmap2024.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.activity.inventory.InventoryActivity
import edu.uark.ahnelson.openstreetmap2024.activity.main.MapsActivity
import edu.uark.ahnelson.openstreetmap2024.activity.settings.SettingsActivity

class BottomNavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_bar, container, false)
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle menu item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> navigateToActivity(MapsActivity::class.java)
                R.id.nav_inventory -> navigateToActivity(InventoryActivity::class.java)
                R.id.nav_settings-> navigateToActivity(SettingsActivity::class.java)
            }
            true
        }

        return view
    }

    private fun navigateToActivity(activityClass: Class<out AppCompatActivity>) {
        val intent = Intent(requireContext(), activityClass)
        startActivity(intent)
        requireActivity().finish() // Finish the current activity
    }
}