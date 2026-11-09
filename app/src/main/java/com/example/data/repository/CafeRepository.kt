package com.example.data.repository

import com.example.data.local.CafeDatabase
import com.example.data.local.entity.ApplicationRecordEntity
import com.example.data.local.entity.CafeSettingsEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class CafeRepository(private val database: CafeDatabase) {
    private val customerDao = database.customerDao()
    private val appDao = database.applicationRecordDao()
    private val transDao = database.transactionDao()
    private val settingsDao = database.cafeSettingsDao()

    // Customers
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    fun getCustomerCount(): Flow<Int> = customerDao.getCustomerCount()
    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Applications
    fun getAllApplications(): Flow<List<ApplicationRecordEntity>> = appDao.getAllApplications()
    fun getApplicationsByCustomer(customerId: Long): Flow<List<ApplicationRecordEntity>> = appDao.getApplicationsByCustomer(customerId)
    fun getTotalApplicationsCount(): Flow<Int> = appDao.getTotalApplicationsCount()
    fun getCompletedApplicationsCount(): Flow<Int> = appDao.getCompletedApplicationsCount()
    fun getPendingApplicationsCount(): Flow<Int> = appDao.getPendingApplicationsCount()
    fun getRecentApplications(limit: Int = 5): Flow<List<ApplicationRecordEntity>> = appDao.getRecentApplications(limit)
    suspend fun insertApplication(app: ApplicationRecordEntity): Long = appDao.insertApplication(app)
    suspend fun updateApplication(app: ApplicationRecordEntity) = appDao.updateApplication(app)
    suspend fun deleteApplication(app: ApplicationRecordEntity) = appDao.deleteApplication(app)

    // Accounting & Transactions
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transDao.getAllTransactions()
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> = transDao.getRecentTransactions(limit)
    fun getTotalIncome(): Flow<Double> = transDao.getTotalIncome()
    fun getTotalExpense(): Flow<Double> = transDao.getTotalExpense()

    fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getMonthStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTodayIncome(): Flow<Double> = transDao.getIncomeSince(getTodayStartMillis())
    fun getTodayExpense(): Flow<Double> = transDao.getExpenseSince(getTodayStartMillis())
    fun getMonthIncome(): Flow<Double> = transDao.getIncomeSince(getMonthStartMillis())

    suspend fun insertTransaction(transaction: TransactionEntity): Long = transDao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = transDao.deleteTransaction(transaction)

    // Settings
    fun getSettings(): Flow<CafeSettingsEntity?> = settingsDao.getSettings()
    suspend fun getSettingsSnapshot(): CafeSettingsEntity = settingsDao.getSettingsSnapshot() ?: CafeSettingsEntity()
    suspend fun updateSettings(settings: CafeSettingsEntity) = settingsDao.insertOrUpdate(settings)

    // Backup & Restore
    suspend fun exportDataAsJson(): String {
        val customers = customerDao.getAllCustomers().first()
        val apps = appDao.getAllApplications().first()
        val trans = transDao.getAllTransactions().first()
        val settings = getSettingsSnapshot()

        val root = JSONObject()
        root.put("exportTime", System.currentTimeMillis())
        root.put("version", 1)

        val settingsObj = JSONObject().apply {
            put("businessName", settings.businessName)
            put("tagline", settings.tagline)
            put("ownerName", settings.ownerName)
            put("mobile", settings.mobile)
            put("address", settings.address)
            put("upiId", settings.upiId)
            put("printRateBwSingle", settings.printRateBwSingle)
            put("printRateColor", settings.printRateColor)
        }
        root.put("settings", settingsObj)

        val custArray = JSONArray()
        customers.forEach { c ->
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("mobile", c.mobile)
                put("email", c.email)
                put("address", c.address)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            }
            custArray.put(cObj)
        }
        root.put("customers", custArray)

        val appArray = JSONArray()
        apps.forEach { a ->
            val aObj = JSONObject().apply {
                put("id", a.id)
                put("customerId", a.customerId)
                put("customerName", a.customerName)
                put("customerMobile", a.customerMobile)
                put("serviceCategory", a.serviceCategory)
                put("serviceName", a.serviceName)
                put("referenceNumber", a.referenceNumber)
                put("status", a.status)
                put("serviceCharge", a.serviceCharge)
                put("paidAmount", a.paidAmount)
                put("paymentStatus", a.paymentStatus)
                put("paymentMethod", a.paymentMethod)
                put("notes", a.notes)
                put("createdAt", a.createdAt)
            }
            appArray.put(aObj)
        }
        root.put("applications", appArray)

        val transArray = JSONArray()
        trans.forEach { t ->
            val tObj = JSONObject().apply {
                put("id", t.id)
                put("type", t.type)
                put("category", t.category)
                put("amount", t.amount)
                put("description", t.description)
                put("paymentMethod", t.paymentMethod)
                put("customerName", t.customerName)
                put("dateMillis", t.dateMillis)
            }
            transArray.put(tObj)
        }
        root.put("transactions", transArray)

        return root.toString(2)
    }

    suspend fun restoreDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("customers")) {
                val custArray = root.getJSONArray("customers")
                for (i in 0 until custArray.length()) {
                    val cObj = custArray.getJSONObject(i)
                    customerDao.insertCustomer(
                        CustomerEntity(
                            id = cObj.optLong("id", 0),
                            name = cObj.getString("name"),
                            mobile = cObj.getString("mobile"),
                            email = cObj.optString("email", ""),
                            address = cObj.optString("address", ""),
                            notes = cObj.optString("notes", ""),
                            createdAt = cObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("applications")) {
                val appArray = root.getJSONArray("applications")
                for (i in 0 until appArray.length()) {
                    val aObj = appArray.getJSONObject(i)
                    appDao.insertApplication(
                        ApplicationRecordEntity(
                            id = aObj.optLong("id", 0),
                            customerId = aObj.getLong("customerId"),
                            customerName = aObj.getString("customerName"),
                            customerMobile = aObj.getString("customerMobile"),
                            serviceCategory = aObj.getString("serviceCategory"),
                            serviceName = aObj.getString("serviceName"),
                            referenceNumber = aObj.optString("referenceNumber", ""),
                            status = aObj.optString("status", "PENDING"),
                            serviceCharge = aObj.optDouble("serviceCharge", 0.0),
                            paidAmount = aObj.optDouble("paidAmount", 0.0),
                            paymentStatus = aObj.optString("paymentStatus", "PAID"),
                            paymentMethod = aObj.optString("paymentMethod", "CASH"),
                            notes = aObj.optString("notes", ""),
                            createdAt = aObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("transactions")) {
                val transArray = root.getJSONArray("transactions")
                for (i in 0 until transArray.length()) {
                    val tObj = transArray.getJSONObject(i)
                    transDao.insertTransaction(
                        TransactionEntity(
                            id = tObj.optLong("id", 0),
                            type = tObj.getString("type"),
                            category = tObj.getString("category"),
                            amount = tObj.getDouble("amount"),
                            description = tObj.optString("description", ""),
                            paymentMethod = tObj.optString("paymentMethod", "CASH"),
                            customerName = tObj.optString("customerName", null),
                            dateMillis = tObj.optLong("dateMillis", System.currentTimeMillis())
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
