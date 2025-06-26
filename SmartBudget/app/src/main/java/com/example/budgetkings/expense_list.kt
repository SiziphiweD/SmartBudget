package com.example.SmartBudget

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabFilter: FloatingActionButton
    private lateinit var adapter: ExpenseAdapter

    private val expenses = mutableListOf<Expense>()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val s = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(s.left, s.top, s.right, s.bottom)
            insets
        }

        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.rvExpenses)
        fabFilter = findViewById(R.id.fabFilter)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(expenses)
        recyclerView.adapter = adapter

        loadExpenses()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterExpensesByRange("week")
                    1 -> filterExpensesByRange("month")
                    2 -> showCustomRangeDialog()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        fabFilter.setOnClickListener {
            Toast.makeText(this, "Filter feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExpenses() {
        if (userId == null) return

        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                expenses.clear()
                for (doc in result) {
                    val expense = doc.toObject(Expense::class.java)
                    expenses.add(expense)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterExpensesByRange(range: String) {
        if (userId == null) return

        val calendar = Calendar.getInstance()

        val startTime = when (range) {
            "week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }

        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                expenses.clear()
                for (doc in result) {
                    val expense = doc.toObject(Expense::class.java)
                    expenses.add(expense)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to filter: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showCustomRangeDialog() {
        val calendarStart = Calendar.getInstance()
        val calendarEnd = Calendar.getInstance()

        DatePickerDialog(this, { _, yearStart, monthStart, dayStart ->
            calendarStart.set(yearStart, monthStart, dayStart, 0, 0, 0)

            DatePickerDialog(this, { _, yearEnd, monthEnd, dayEnd ->
                calendarEnd.set(yearEnd, monthEnd, dayEnd, 23, 59, 59)
                filterExpensesByCustomRange(calendarStart.timeInMillis, calendarEnd.timeInMillis)
            },
                calendarEnd.get(Calendar.YEAR),
                calendarEnd.get(Calendar.MONTH),
                calendarEnd.get(Calendar.DAY_OF_MONTH)
            ).show()

        },
            calendarStart.get(Calendar.YEAR),
            calendarStart.get(Calendar.MONTH),
            calendarStart.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun filterExpensesByCustomRange(startTimestamp: Long, endTimestamp: Long) {
        if (userId == null) return

        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
            .whereLessThanOrEqualTo("timestamp", endTimestamp)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                expenses.clear()
                for (doc in result) {
                    val expense = doc.toObject(Expense::class.java)
                    expenses.add(expense)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load custom range: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
