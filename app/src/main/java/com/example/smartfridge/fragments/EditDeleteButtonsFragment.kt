package com.example.smartfridge.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.smartfridge.R
import com.example.smartfridge.activities.EditIngredientActivity
import com.example.smartfridge.activities.ManageIngredientsActivity
import com.example.smartfridge.models.IngredientModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditDeleteButtonsFragment : Fragment() {
    private lateinit var btnFragmentEdit : Button
    private lateinit var btnFragmentDelete : Button

    private lateinit var txtPreviewName : TextView
    private lateinit var txtPreviewCategory : TextView
    private lateinit var txtPreviewStock : TextView

    // db for delete operation
    private var db = Firebase.firestore
    private lateinit var storageRef : StorageReference

    private var currentIng : IngredientModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_delete_buttons, container, false)
        btnFragmentEdit = view.findViewById(R.id.btn_fragment_edit)
        btnFragmentDelete = view.findViewById(R.id.btn_fragment_delete)

        txtPreviewName = view.findViewById(R.id.txt_preview_name)
        txtPreviewCategory = view.findViewById(R.id.txt_preview_category)
        txtPreviewStock = view.findViewById(R.id.txt_preview_stock)

        return view
    }

    fun updateCurrentIng(ing: IngredientModel) {
        if (currentIng == null) {
            // Enable buttons
            btnFragmentEdit.isEnabled = true
            btnFragmentDelete.isEnabled = true
        }
        currentIng = ing

        txtPreviewName.text = currentIng?.ingName
        txtPreviewCategory.text = currentIng?.ingCategory
        txtPreviewStock.text = currentIng?.ingStock
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFragmentEdit.setOnClickListener {
            // Send Intent to Edit Activity
            val intent = Intent(context, EditIngredientActivity::class.java)
            intent.putExtra("ingId", currentIng?.ingId.toString())
            intent.putExtra("ingName", currentIng?.ingName.toString())
            intent.putExtra("ingCategory", currentIng?.ingCategory.toString())
            intent.putExtra("ingStock", currentIng?.ingStock.toString())
            intent.putExtra("ingImageUrl", currentIng?.ingImageUrl.toString())
            intent.putExtra("ingDate", currentIng?.ingDate.toString())
            startActivity(intent)
        }

        btnFragmentDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    // Delete from database
                    storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentIng?.ingImageUrl.toString())
                    storageRef.delete().addOnSuccessListener {
                        db.collection("ingredients").document(currentIng?.ingId.toString())
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Ingredient has been removed successfully", Toast.LENGTH_LONG).show()
                                (activity as? ManageIngredientsActivity)?.updateItems() // Calls to rebuild list fragment

                                // Clean previews
                                txtPreviewName.text = "Preview name"
                                txtPreviewCategory.text = "Preview category"
                                txtPreviewStock.text = "Preview stock"

                                // Set the current ing to null
                                currentIng = null

                                // Disable buttons
                                btnFragmentEdit.isEnabled = false
                                btnFragmentDelete.isEnabled = false
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error while removing ingredient: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error while removing ingredient image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }
}