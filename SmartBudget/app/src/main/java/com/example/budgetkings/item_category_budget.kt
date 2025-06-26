package com.example.SmartBudget

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryBudgetAdapter(
    private val items: List<CategoryBudget>
) : RecyclerView.Adapter<CategoryBudgetAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_category_budget, parent, false)
        ) {
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategoryName)
        val etAmount: EditText = itemView.findViewById(R.id.etCategoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = items[position]
        holder.txtCategory.text = item.categoryName
        holder.etAmount.setText(item.budgetAmount.toString())

        holder.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val value = s.toString().toDoubleOrNull() ?: 0.0
                    items[adapterPosition].budgetAmount = value
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = items.size

    fun getCategoryBudgets(): List<CategoryBudget> = items
}
