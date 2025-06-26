package com.example.SmartBudget

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 100
        private const val REQUEST_IMAGE_PICK = 101
    }

    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var ivReceipt: ImageView
    private lateinit var fabTakePhoto: FloatingActionButton
    private lateinit var btnSave: Button

    private val calendar = Calendar.getInstance()
    private var receiptImageUri: Uri? = null
    private var receiptBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val s = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(s.left, s.top, s.right, s.bottom)
            insets
        }

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        actvCategory = findViewById(R.id.actvCategory)
        ivReceipt = findViewById(R.id.ivReceipt)
        fabTakePhoto = findViewById(R.id.fabTakePhoto)
        btnSave = findViewById(R.id.btnSaveExpense)

        fabTakePhoto.setOnClickListener { dispatchTakePictureIntent() }
        ivReceipt.setOnClickListener { pickImageFromGallery() }

        etDate.setOnClickListener { showDatePicker() }
        btnSave.setOnClickListener {
            if (validateExpense()) saveExpense()
        }

        setupCategoryDropdown()
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Groceries", "Transport", "Rent", "Entertainment")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)
        actvCategory.inputType = 0
        actvCategory.setOnClickListener { actvCategory.showDropDown() }
    }

    private fun showDatePicker() {
        DatePickerDialog(this,
            { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(format.format(calendar.time))
    }

    private fun validateExpense(): Boolean {
        return when {
            etAmount.text.isNullOrBlank() -> { etAmount.error = "Enter amount"; false }
            etDescription.text.isNullOrBlank() -> { etDescription.error = "Enter description"; false }
            etDate.text.isNullOrBlank() -> { etDate.error = "Select a date"; false }
            actvCategory.text.isNullOrBlank() -> { actvCategory.error = "Select category"; false }
            else -> true
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            startActivityForResult(it, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                receiptBitmap = data?.extras?.get("data") as Bitmap
                ivReceipt.setImageBitmap(receiptBitmap)
            }
            REQUEST_IMAGE_PICK -> {
                receiptImageUri = data?.data
                ivReceipt.setImageURI(receiptImageUri)
            }
        }
    }

    private fun saveExpense() {
        val amount = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val date = etDate.text.toString().trim()
        val category = actvCategory.text.toString().trim()
        val timestamp = System.currentTimeMillis()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (receiptBitmap != null || receiptImageUri != null) {
            uploadReceiptAndSave(amount, description, date, category, timestamp, userId)
        } else {
            saveExpenseToFirestore(amount, description, date, category, null, timestamp, userId)
        }
    }

    private fun uploadReceiptAndSave(
        amount: String, description: String, date: String,
        category: String, timestamp: Long, userId: String
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("receipts/$userId/$timestamp.jpg")

        val uploadTask = when {
            receiptImageUri != null -> storageRef.putFile(receiptImageUri!!)
            receiptBitmap != null -> {
                val baos = ByteArrayOutputStream()
                receiptBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                val data = baos.toByteArray()
                storageRef.putBytes(data)
            }
            else -> return
        }

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveExpenseToFirestore(amount, description, date, category, uri.toString(), timestamp, userId)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveExpenseToFirestore(
        amount: String, description: String, date: String,
        category: String, receiptUrl: String?, timestamp: Long, userId: String
    ) {
        val data = hashMapOf(
            "amount" to amount,
            "description" to description,
            "date" to date,
            "category" to category,
            "receiptUrl" to receiptUrl,
            "timestamp" to timestamp,
            "userId" to userId
        )

        FirebaseFirestore.getInstance().collection("expenses").add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
