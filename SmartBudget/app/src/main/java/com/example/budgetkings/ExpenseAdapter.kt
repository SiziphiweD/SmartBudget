package com.example.SmartBudget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExpenseAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val ivReceiptThumb: ImageView = itemView.findViewById(R.id.ivReceiptThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.txtTitle.text = "${expense.description} (${expense.category})"
        holder.txtAmount.text = "R${expense.amount}"

        if (!expense.receiptUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView)
                .load(expense.receiptUrl)
                .into(holder.ivReceiptThumb)
        } else {
            holder.ivReceiptThumb.setImageResource(R.drawable.image_placeholder) // Optional default
        }
    }

    override fun getItemCount(): Int = expenses.size
}
