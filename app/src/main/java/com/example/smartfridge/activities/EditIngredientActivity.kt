package com.example.smartfridge.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartfridge.R
import com.example.smartfridge.models.IngredientModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditIngredientActivity : AppCompatActivity() {
    private lateinit var edtxtName : EditText
    private lateinit var edtxtCategory : EditText
    private lateinit var edtxtStock : EditText
    private lateinit var btnEdit : Button
    private lateinit var imgPrev : ImageView
    private lateinit var txtDatePrev : TextView

    // Needed to reference prev status
    private var ingId : String? = null
    private var ingImageUrl : String? = null

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
        setContentView(R.layout.activity_edit_ingredient)

        // Define view-controller relations
        edtxtName = findViewById(R.id.edit_act_edtxt_name)
        edtxtCategory = findViewById(R.id.edit_act_edtxt_category)
        btnEdit = findViewById(R.id.btn_edit)
        edtxtStock = findViewById(R.id.edit_act_edtxt_stock)
        imgPrev = findViewById(R.id.edit_act_img_prev)
        txtDatePrev = findViewById(R.id.edit_txt_preview_date)

        // Storage (images) Reference
        storageRef = FirebaseStorage.getInstance().getReference("images")

        // Load values from Intent
        setValuesToViews()

        // Start date picker dialog
        txtDatePrev.setOnClickListener {
            showDatePicker()
        }

        // Send intent to GALLERY
        imgPrev.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnEdit.setOnClickListener {
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

    private fun setValuesToViews() {
        ingId = intent.getStringExtra("ingId")
        edtxtName.setText(intent.getStringExtra("ingName"))
        edtxtCategory.setText(intent.getStringExtra("ingCategory"))
        edtxtStock.setText(intent.getStringExtra("ingStock"))

        // Load image
        ingImageUrl = intent.getStringExtra("ingImageUrl")
        Picasso.get().load(ingImageUrl).into(imgPrev)

        // Load date dialog
        calendar = Calendar.getInstance()
        val ingDate = intent.getStringExtra("ingDate")
        if (ingDate != null) {
            txtDatePrev.text = ingDate
            val formatter : DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            val date = formatter.parse(ingDate.toString())
            if (date != null) {
                calendar.time = date
            }
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
        val stock = edtxtStock.text.toString()
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

        if (stock.isEmpty()) {
            edtxtStock.error = "Stock is required"
            flag = true
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
            // Then image has changed. Remove previous image
            storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(ingImageUrl.toString())
            storageRef.delete().addOnSuccessListener {
                // Request a new image ID
                val imageRef = storageRef.child("$imageId.jpg")
                imageRef.putFile(uri!!)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Save ingredient to Firestore
                            db.collection("ingredients")
                                .document(ingId.toString()).update(IngredientModel(ingId, name, category, stock, uri.toString(), date).toMap())
                                .addOnSuccessListener { _ ->
                                    Toast.makeText(applicationContext, "Ingredient successfully updated", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(applicationContext, "Error while updating ingredient: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(applicationContext, "Error while uploading new image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }

        } else {
            // We don't have to change the image. Logic without removing original image
            // Save ingredient to Firestore
            db.collection("ingredients")
                .document(ingId.toString()).update(IngredientModel(ingId, name, category, stock, ingImageUrl, date).toMap())
                .addOnSuccessListener { _ ->
                    Toast.makeText(applicationContext, "Ingredient successfully updated", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(applicationContext, "Error while updating ingredient: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getImageId() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..20)
            .map { allowedChars.random() }
            .joinToString("")
    }
}