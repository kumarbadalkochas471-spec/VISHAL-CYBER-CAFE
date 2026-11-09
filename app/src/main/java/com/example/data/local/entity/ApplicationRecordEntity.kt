package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "application_records")
data class ApplicationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerMobile: String,
    val serviceCategory: String, // e.g., Aadhaar, PAN Card, Voter ID, Bihar RTPS, Govt Jobs, Scholarship, University, CSC
    val serviceName: String,     // e.g., New PAN Card, Bihar Niwas Praman Patra, SSC CGL
    val referenceNumber: String = "", // Ack / Reg / Roll / App No
    val trackingId: String = "",      // User ID or Login token
    val status: String = "PENDING",   // PENDING, IN_PROGRESS, SUBMITTED, COMPLETED, REJECTED
    val serviceCharge: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentStatus: String = "PENDING", // PAID, PARTIAL, DUE
    val paymentMethod: String = "CASH",    // CASH, UPI
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
