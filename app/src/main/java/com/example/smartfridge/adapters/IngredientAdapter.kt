package com.example.smartfridge.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge.R
import com.example.smartfridge.models.IngredientModel

class IngredientAdapter (
    var ingredients : ArrayList<IngredientModel>,
    private val onItemClicked : (IngredientModel) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_ingredients, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IngredientAdapter.ViewHolder, position: Int) {
        val currentIng = ingredients[position]
        holder.txtIngName.text = currentIng.ingName
        holder.itemView.setOnClickListener {
            onItemClicked(currentIng)
        }
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtIngName: TextView = itemView.findViewById(R.id.txt_item_ing_name)
    }
}
