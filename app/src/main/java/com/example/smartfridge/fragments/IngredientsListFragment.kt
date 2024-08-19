package com.example.smartfridge.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge.R
import com.example.smartfridge.activities.ManageIngredientsActivity
import com.example.smartfridge.adapters.IngredientAdapter
import com.example.smartfridge.models.IngredientModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class IngredientsListFragment : Fragment() {
    private lateinit var rvManager : RecyclerView
    private lateinit var txtLoading : TextView

    private lateinit var ingredients : ArrayList<IngredientModel>
    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ingredients_list, container, false)
        ingredients = arrayListOf<IngredientModel>()

        rvManager = view.findViewById(R.id.rv_manager)
        rvManager.layoutManager = LinearLayoutManager(context)
        rvManager.setHasFixedSize(true)

        txtLoading = view.findViewById(R.id.txt_loading)

        getIngredients()
        return view
    }

    private fun getIngredients() {
        rvManager.visibility = View.GONE
        txtLoading.visibility = View.VISIBLE

        db.collection("ingredients")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val ingredient = IngredientModel()
                    ingredient.fromMap(document.data, document.id)
                    ingredients.add(ingredient)
                }

                // Sort ingredients
                ingredients.sortWith(compareByDescending { it.ingName })
                ingredients.reverse()

                if (rvManager.adapter == null) {
                    // Define handler for on click listener on item inside adapter (runs the activity method)
                    val mAdapter = IngredientAdapter(ingredients) { currentIng ->
                        (activity as? ManageIngredientsActivity)?.updateCurrentIng(currentIng)
                    }
                    rvManager.adapter = mAdapter
                } else {
                    rvManager.adapter?.notifyItemRangeInserted(0, ingredients.size)
                }

                rvManager.visibility = View.VISIBLE
                txtLoading.visibility = View.GONE
            }
    }
}