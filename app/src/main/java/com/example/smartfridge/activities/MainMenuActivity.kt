package com.example.smartfridge.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfridge.R

class MainMenuActivity : AppCompatActivity() {
    private lateinit var btnIntentInsert : Button
    private lateinit var btnIntentManage : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Define view-controller relations
        btnIntentInsert = findViewById(R.id.btn_intent_insert)
        btnIntentManage = findViewById(R.id.btn_intent_manage)

        btnIntentInsert.setOnClickListener {
            val intent = Intent(this, InsertIngredientsActivity::class.java)
            startActivity(intent)
        }

        btnIntentManage.setOnClickListener {
            val intent = Intent(this, ManageIngredientsActivity::class.java)
            startActivity(intent)
        }
    }
}