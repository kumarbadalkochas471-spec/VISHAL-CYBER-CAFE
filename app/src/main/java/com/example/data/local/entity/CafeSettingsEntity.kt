package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cafe_settings")
data class CafeSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "VISHAL CYBER CAFE & CSC CENTRE",
    val tagline: String = "CSC Digital Seva Kendra & All Online Services",
    val ownerName: String = "Vishal Kumar",
    val mobile: String = "9876543210",
    val email: String = "vishalcybercafe.kochas@gmail.com",
    val address: String = "Purana Bazar, Ward No. 16, Kochas, Rohtas, Bihar – 821112",
    val upiId: String = "vishalcyber@upi",
    val pinLockEnabled: Boolean = false,
    val securityPin: String = "1234",
    val printRateBwSingle: Double = 5.0,
    val printRateBwDouble: Double = 8.0,
    val printRateColor: Double = 15.0,
    val printRatePhotoSheet: Double = 50.0,
    val laminationRate: Double = 20.0,
    val passportPhotoRate: Double = 40.0
)
