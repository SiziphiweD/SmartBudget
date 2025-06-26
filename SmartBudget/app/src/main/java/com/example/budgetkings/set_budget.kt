package com.example.SmartBudget

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SetBudgetActivity : AppCompatActivity() {

    private lateinit var etMonthlyBudget: TextInputEditText
    private lateinit var rvCategoryBudgets: RecyclerView
    private lateinit var btnSaveBudget: Button

    private val categoryList = mutableListOf<CategoryBudget>()
    private lateinit var adapter: CategoryBudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_budget)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etMonthlyBudget = findViewById(R.id.etMonthlyBudget)
        rvCategoryBudgets = findViewById(R.id.rvCategoryBudgets)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)

        categoryList.addAll(getCategoryBudgetList())
        adapter = CategoryBudgetAdapter(categoryList)
        rvCategoryBudgets.layoutManager = LinearLayoutManager(this)
        rvCategoryBudgets.adapter = adapter

        btnSaveBudget.setOnClickListener {
            if (validateBudget()) {
                saveBudgetSettings()
            }
        }

        loadExistingBudgetIfAny()
    }

    private fun validateBudget(): Boolean {
        val monthlyBudget = etMonthlyBudget.text.toString()

        return when {
            monthlyBudget.isEmpty() -> {
                etMonthlyBudget.error = "Please enter monthly budget"
                false
            }
            monthlyBudget.toDoubleOrNull() == null -> {
                etMonthlyBudget.error = "Please enter a valid number"
                false
            }
            monthlyBudget.toDouble() <= 0 -> {
                etMonthlyBudget.error = "Budget must be greater than 0"
                false
            }
            else -> {
                etMonthlyBudget.error = null
                true
            }
        }
    }

    private fun saveBudgetSettings() {
        val monthlyBudget = etMonthlyBudget.text.toString().toDouble()
        val categoryBudgets = categoryList.associate { it.categoryName to it.budgetAmount }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val budgetData = mapOf(
            "monthlyBudget" to monthlyBudget,
            "categoryBudgets" to categoryBudgets,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("budgets")
            .document(userId)
            .set(budgetData)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadExistingBudgetIfAny() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("budgets")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val monthlyBudget = doc.getDouble("monthlyBudget") ?: 0.0
                    etMonthlyBudget.setText(monthlyBudget.toString())

                    val catMap = doc.get("categoryBudgets") as? Map<String, Number>
                    catMap?.forEach { (name, amount) ->
                        categoryList.find { it.categoryName == name }?.budgetAmount = amount.toDouble()
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun getCategoryBudgetList(): List<CategoryBudget> {
        return listOf(
            CategoryBudget("Food", 0.0),
            CategoryBudget("Transport", 0.0),
            CategoryBudget("Entertainment", 0.0),
            CategoryBudget("Utilities", 0.0),
            CategoryBudget("Health", 0.0),
            CategoryBudget("Savings", 0.0),
            CategoryBudget("Other", 0.0)
        )
    }
}

data class CategoryBudget(val categoryName: String, var budgetAmount: Double)
