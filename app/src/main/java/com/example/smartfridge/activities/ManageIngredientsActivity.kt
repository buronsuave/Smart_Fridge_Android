package com.example.smartfridge.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge.R
import com.example.smartfridge.adapters.IngredientAdapter
import com.example.smartfridge.fragments.EditDeleteButtonsFragment
import com.example.smartfridge.fragments.IngredientsListFragment
import com.example.smartfridge.models.IngredientModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ManageIngredientsActivity : AppCompatActivity() {
    private lateinit var rvFragment : IngredientsListFragment
    private lateinit var btnsFragment : EditDeleteButtonsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_ingredients)

        rvFragment = IngredientsListFragment()
        btnsFragment = EditDeleteButtonsFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_list_container, rvFragment)
            .replace(R.id.fragment_buttons_container, btnsFragment)
            .commit()
    }

    fun updateCurrentIng(ingredient: IngredientModel) {
        btnsFragment.updateCurrentIng(ingredient)
    }

    fun updateItems() {
        rvFragment = IngredientsListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_list_container, rvFragment)
            .commit()
    }
}