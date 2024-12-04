package edu.uark.ahnelson.openstreetmap2024.activity.inventory

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.uark.ahnelson.openstreetmap2024.R
import edu.uark.ahnelson.openstreetmap2024.data.entity.MintedToken

class InventoryAdapter(private val tokens: List<MintedToken>) :
    RecyclerView.Adapter<InventoryAdapter.TokenViewHolder>() {

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewToken: ImageView = itemView.findViewById(R.id.imageViewToken)
        val textViewMintNumber: TextView = itemView.findViewById(R.id.textViewMintNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_token, parent, false)
        Log.d("InventoryAdapter", "$tokens")
        return TokenViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val token = tokens[position]

        // Set the mint number
        holder.textViewMintNumber.text = "Mint #${token.mintNum}"

        // Load and set the token image manually
        setPic(holder.imageViewToken, token.tokenId)
    }

    override fun getItemCount(): Int = tokens.size

    /**
     * Decodes and sets an image to the ImageView manually using BitmapFactory.
     */
    private fun setPic(imageView: ImageView, tokenId: Int) {
        // Get the drawable resource
        val drawable = when (tokenId) {
            1 -> R.drawable.mind_pin
            2 -> R.drawable.muscle_pin
            3 -> R.drawable.planet_pin
            4 -> R.drawable.robot_pin
            5 -> R.drawable.shield_pin
            else -> R.drawable.robot_pin
        }

        val bitmap = (ContextCompat.getDrawable(imageView.context, drawable) as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            // Set the Bitmap to the ImageView
            imageView.setImageBitmap(bitmap)
        } else {
            Log.e("InventoryAdapter", "Failed to load image for token ID $tokenId")
        }
    }


}
