package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String, // Income: Form Application, Photo Print, Xerox/Print, Scanning, Lamination, Online Service. Expense: Paper, Ink, Electricity, Wifi, Rent, Tea/Snacks, Maintenance
    val amount: Double,
    val description: String = "",
    val paymentMethod: String = "CASH", // "CASH", "UPI"
    val customerId: Long? = null,
    val customerName: String? = null,
    val dateMillis: Long = System.currentTimeMillis()
)
