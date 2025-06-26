package com.example.SmartBudget

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportActivity : AppCompatActivity() {

    private lateinit var tvMonthlyBudget: TextView
    private lateinit var tvCategoryBreakdown: TextView
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget)
        tvCategoryBreakdown = findViewById(R.id.tvCategoryBreakdown)
        pieChart = findViewById(R.id.pieChart)

        fetchReportData()
    }

    private fun fetchReportData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("budgets")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val monthlyBudget = doc.getDouble("monthlyBudget") ?: 0.0
                    val categories = doc.get("categoryBudgets") as? Map<*, *>

                    tvMonthlyBudget.text = "Monthly Budget: R%.2f".format(monthlyBudget)

                    val report = StringBuilder()
                    val entries = ArrayList<PieEntry>()

                    categories?.forEach { (category, amount) ->
                        val amt = (amount as Number).toFloat()
                        entries.add(PieEntry(amt, category.toString()))
                        report.append("${category.toString()}: R%.2f\n".format(amt))
                    }

                    tvCategoryBreakdown.text = report.toString()

                    val dataSet = PieDataSet(entries, "Category Breakdown")
                    dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                    dataSet.valueTextColor = Color.BLACK
                    dataSet.valueTextSize = 14f

                    val pieData = PieData(dataSet)
                    pieChart.data = pieData
                    pieChart.description.isEnabled = false
                    pieChart.setEntryLabelTextSize(12f)
                    pieChart.setUsePercentValues(true)
                    pieChart.centerText = "Budget Allocation"
                    pieChart.invalidate()
                } else {
                    Toast.makeText(this, "No budget data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load report: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
