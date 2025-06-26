package com.example.SmartBudget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Dashboard : AppCompatActivity() {

    private lateinit var btnAddExpense: MaterialButton
    private lateinit var btnViewExpenses: MaterialButton
    private lateinit var btnSetBudget: MaterialButton
    private lateinit var btnViewReports: MaterialButton
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var rvRecentExpenses: RecyclerView
    private lateinit var btnLogout: Button

    private lateinit var adapter: ExpenseAdapter
    private val recentExpenses = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        btnAddExpense = findViewById(R.id.btnAddExpense)
        btnViewExpenses = findViewById(R.id.btnViewExpenses)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        btnViewReports = findViewById(R.id.btnViewReports)
        fabAdd = findViewById(R.id.fabAdd)
        rvRecentExpenses = findViewById(R.id.rvRecentExpenses)
        btnLogout = findViewById(R.id.btnLogout)

        // Setup RecyclerView
        rvRecentExpenses.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(recentExpenses)
        rvRecentExpenses.adapter = adapter

        // Load Firestore data
        loadRecentExpenses()

        // Navigation and actions
        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        btnViewExpenses.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        btnSetBudget.setOnClickListener {
            startActivity(Intent(this, SetBudgetActivity::class.java))
        }

        btnViewReports.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }


        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadRecentExpenses(limit: Long = 5) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { result ->
                recentExpenses.clear()
                for (doc in result) {
                    val expense = doc.toObject(Expense::class.java)
                    recentExpenses.add(expense)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
