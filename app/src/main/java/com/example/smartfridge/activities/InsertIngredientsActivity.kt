package com.example.smartfridge.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfridge.models.IngredientModel
import com.example.smartfridge.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InsertIngredientsActivity : AppCompatActivity() {
    private lateinit var edtxtName : EditText
    private lateinit var edtxtCategory : EditText
    private lateinit var chkStock : CheckBox
    private lateinit var edtxtStock : EditText
    private lateinit var btnAdd : Button
    private lateinit var imgPrev : ImageView
    private lateinit var txtDatePrev : TextView

    // For Gallery Intent
    private val PICK_IMAGE_REQUEST = 1
    private var uri : Uri? = null

    // For Date Picker
    private val DEFAULT_DATE_STRING : String = "Click to select date"
    private lateinit var calendar : Calendar

    // Database variables
    private var db = Firebase.firestore
    private lateinit var storageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_ingredients)

        // Define view-controller relations
        edtxtName = findViewById(R.id.edtxt_name)
        edtxtCategory = findViewById(R.id.edtxt_category)
        chkStock = findViewById(R.id.chk_stock)
        btnAdd = findViewById(R.id.btn_add)
        edtxtStock = findViewById(R.id.edtxt_stock)
        imgPrev = findViewById(R.id.add_img_prev)
        txtDatePrev = findViewById(R.id.add_txt_preview_date)

        // Storage (images) Reference
        storageRef = FirebaseStorage.getInstance().getReference("images")

        chkStock.setOnCheckedChangeListener { _, b ->
            edtxtStock.isEnabled = b
        }
        // Send intent to GALLERY
        imgPrev.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Start date picker dialog
        calendar = Calendar.getInstance()
        txtDatePrev.setOnClickListener {
            showDatePicker()
        }

        btnAdd.setOnClickListener {
            saveIngredient()
        }
    }

    // After receiving result from GALLERY intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            uri = data.data
            imgPrev.setImageURI(uri)
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, monthOfYear, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            txtDatePrev.text = formattedDate.toString()
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun saveIngredient(){
        // Retrieve values and basic validation
        val name = edtxtName.text.toString()
        val category = edtxtCategory.text.toString()
        val needsStock = chkStock.isChecked
        var stock = edtxtStock.text.toString()
        val date = txtDatePrev.text.toString()
        var flag =  false

        if (name.isEmpty()) {
            edtxtName.error = "Name is required"
            flag = true
        }

        if (category.isEmpty()) {
            edtxtCategory.error = "Category is required"
            flag = true
        }

        if (needsStock) {
            if (stock.isEmpty()) {
                edtxtStock.error = "Stock is checked"
                flag = true
            }
        } else {
            stock = "0"
        }

        if (date == DEFAULT_DATE_STRING) {
            Toast.makeText(applicationContext, "You must select a date to continue", Toast.LENGTH_LONG).show()
            flag = true
        }

        if (flag) {
            return
        }

        val imageId = getImageId()
        if (uri != null) {
            val imageRef = storageRef.child("$imageId.jpg")
            imageRef.putFile(uri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Save ingredient to Firestore
                        db.collection("ingredients")
                            .add(IngredientModel(null, name, category, stock, uri.toString(), date).toMap())
                            .addOnSuccessListener { a ->
                                Toast.makeText(applicationContext, "Ingredient successfully added", Toast.LENGTH_LONG).show()
                                // Clear fields
                                edtxtName.text.clear()
                                edtxtCategory.text.clear()
                                edtxtStock.text.clear()
                                chkStock.isChecked = false
                                imgPrev.setImageResource(R.drawable.ic_empty_ingredient)
                                txtDatePrev.text = DEFAULT_DATE_STRING

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(applicationContext, "Error while adding ingredient: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(applicationContext, "Error while uploading image: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(applicationContext, "Please select an image", Toast.LENGTH_LONG).show()
        }
    }

    private fun getImageId() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..20)
            .map { allowedChars.random() }
            .joinToString("")
    }
}