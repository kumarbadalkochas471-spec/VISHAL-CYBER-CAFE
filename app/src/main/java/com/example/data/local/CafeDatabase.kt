package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ApplicationRecordDao
import com.example.data.local.dao.CafeSettingsDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.ApplicationRecordEntity
import com.example.data.local.entity.CafeSettingsEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        ApplicationRecordEntity::class,
        TransactionEntity::class,
        CafeSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CafeDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun applicationRecordDao(): ApplicationRecordDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cafeSettingsDao(): CafeSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CafeDatabase? = null

        fun getDatabase(context: Context): CafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CafeDatabase::class.java,
                    "vishal_cyber_cafe.db"
                )
                    .addCallback(SeedCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }

            private suspend fun seedInitialData(database: CafeDatabase) {
                // Initialize Cafe Settings
                database.cafeSettingsDao().insertOrUpdate(CafeSettingsEntity())

                // Seed Initial Real-world Customers
                val c1 = database.customerDao().insertCustomer(
                    CustomerEntity(
                        name = "Ramesh Kumar Sharma",
                        mobile = "9871234560",
                        address = "Village Parsathua, Kochas",
                        notes = "Frequent customer for job forms"
                    )
                )
                val c2 = database.customerDao().insertCustomer(
                    CustomerEntity(
                        name = "Priya Kumari",
                        mobile = "9123456789",
                        address = "Ward No. 12, Kochas Bazar",
                        notes = "Scholarship and college admission"
                    )
                )
                val c3 = database.customerDao().insertCustomer(
                    CustomerEntity(
                        name = "Manoj Singh",
                        mobile = "9934125678",
                        address = "Village Baligaon, Rohtas",
                        notes = "Land records & PM Kisan"
                    )
                )
                val c4 = database.customerDao().insertCustomer(
                    CustomerEntity(
                        name = "Anjali Gupta",
                        mobile = "8877665544",
                        address = "Near High School, Kochas",
                        notes = "Passport photo & PAN Card"
                    )
                )

                val now = System.currentTimeMillis()
                val oneHourAgo = now - 3600000L
                val threeHoursAgo = now - 10800000L
                val yesterday = now - 86400000L

                // Seed Applications
                database.applicationRecordDao().insertApplication(
                    ApplicationRecordEntity(
                        customerId = c1,
                        customerName = "Ramesh Kumar Sharma",
                        customerMobile = "9871234560",
                        serviceCategory = "Govt Jobs",
                        serviceName = "SSC CGL 2026 Online Form",
                        referenceNumber = "SSC2026BR89214",
                        status = "SUBMITTED",
                        serviceCharge = 150.0,
                        paidAmount = 150.0,
                        paymentStatus = "PAID",
                        paymentMethod = "UPI",
                        notes = "Exam center preference: Patna, Gaya",
                        createdAt = threeHoursAgo,
                        updatedAt = oneHourAgo
                    )
                )

                database.applicationRecordDao().insertApplication(
                    ApplicationRecordEntity(
                        customerId = c2,
                        customerName = "Priya Kumari",
                        customerMobile = "9123456789",
                        serviceCategory = "Scholarship",
                        serviceName = "Bihar Post Matric Scholarship (PMS)",
                        referenceNumber = "PMS26BR77120",
                        status = "IN_PROGRESS",
                        serviceCharge = 120.0,
                        paidAmount = 100.0,
                        paymentStatus = "PARTIAL",
                        paymentMethod = "CASH",
                        notes = "Bonafide certificate verification pending",
                        createdAt = twoHoursAgo(now),
                        updatedAt = now
                    )
                )

                database.applicationRecordDao().insertApplication(
                    ApplicationRecordEntity(
                        customerId = c3,
                        customerName = "Manoj Singh",
                        customerMobile = "9934125678",
                        serviceCategory = "Bihar RTPS",
                        serviceName = "Residential & Income Certificate (Niwas & Aay)",
                        referenceNumber = "BRCCO/2026/91823",
                        status = "COMPLETED",
                        serviceCharge = 100.0,
                        paidAmount = 100.0,
                        paymentStatus = "PAID",
                        paymentMethod = "CASH",
                        notes = "Certificates downloaded and printed",
                        createdAt = yesterday,
                        updatedAt = oneHourAgo
                    )
                )

                database.applicationRecordDao().insertApplication(
                    ApplicationRecordEntity(
                        customerId = c4,
                        customerName = "Anjali Gupta",
                        customerMobile = "8877665544",
                        serviceCategory = "PAN Card",
                        serviceName = "New PAN Card Application (Form 49A)",
                        referenceNumber = "PAN8821934A",
                        status = "PENDING",
                        serviceCharge = 200.0,
                        paidAmount = 200.0,
                        paymentStatus = "PAID",
                        paymentMethod = "UPI",
                        notes = "Aadhaar authentication completed",
                        createdAt = now - 1800000L,
                        updatedAt = now
                    )
                )

                // Seed Transactions (Income & Expenses)
                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        category = "Form Application",
                        amount = 150.0,
                        description = "SSC CGL Form fee (Ramesh Kumar)",
                        paymentMethod = "UPI",
                        customerId = c1,
                        customerName = "Ramesh Kumar Sharma",
                        dateMillis = threeHoursAgo
                    )
                )

                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        category = "Photo Print",
                        amount = 80.0,
                        description = "16 Passport Photos + Name/Date (Anjali Gupta)",
                        paymentMethod = "CASH",
                        customerId = c4,
                        customerName = "Anjali Gupta",
                        dateMillis = twoHoursAgo(now)
                    )
                )

                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        category = "Xerox / Print",
                        amount = 65.0,
                        description = "13 Pages B&W Document Print",
                        paymentMethod = "CASH",
                        dateMillis = oneHourAgo
                    )
                )

                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "INCOME",
                        category = "Online Service",
                        amount = 200.0,
                        description = "New PAN Card Form 49A (Anjali Gupta)",
                        paymentMethod = "UPI",
                        customerId = c4,
                        customerName = "Anjali Gupta",
                        dateMillis = now - 1800000L
                    )
                )

                // Today's Expenses
                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        category = "Paper Ream A4",
                        amount = 280.0,
                        description = "1 Ream JK Copier A4 75GSM",
                        paymentMethod = "CASH",
                        dateMillis = now - 14400000L
                    )
                )

                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        type = "EXPENSE",
                        category = "Tea / Snacks",
                        amount = 50.0,
                        description = "Customer and operator evening tea",
                        paymentMethod = "UPI",
                        dateMillis = now - 7200000L
                    )
                )
            }

            private fun twoHoursAgo(now: Long) = now - 7200000L
        }
    }
}
