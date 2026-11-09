package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ApplicationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationRecordDao {
    @Query("SELECT * FROM application_records ORDER BY createdAt DESC")
    fun getAllApplications(): Flow<List<ApplicationRecordEntity>>

    @Query("SELECT * FROM application_records WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getApplicationsByCustomer(customerId: Long): Flow<List<ApplicationRecordEntity>>

    @Query("SELECT * FROM application_records WHERE status = :status ORDER BY createdAt DESC")
    fun getApplicationsByStatus(status: String): Flow<List<ApplicationRecordEntity>>

    @Query("SELECT COUNT(*) FROM application_records")
    fun getTotalApplicationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM application_records WHERE status = 'COMPLETED'")
    fun getCompletedApplicationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM application_records WHERE status = 'PENDING' OR status = 'IN_PROGRESS'")
    fun getPendingApplicationsCount(): Flow<Int>

    @Query("SELECT * FROM application_records ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentApplications(limit: Int = 5): Flow<List<ApplicationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: ApplicationRecordEntity): Long

    @Update
    suspend fun updateApplication(app: ApplicationRecordEntity)

    @Delete
    suspend fun deleteApplication(app: ApplicationRecordEntity)

    @Query("DELETE FROM application_records")
    suspend fun deleteAll()
}
