package com.example.SmartBudget

data class Expense(
    val amount: String = "",
    val description: String = "",
    val date: String = "",
    val category: String = "",
    val receiptUrl: String? = null,
    val timestamp: Long = 0L,
    val userId: String = ""
)
