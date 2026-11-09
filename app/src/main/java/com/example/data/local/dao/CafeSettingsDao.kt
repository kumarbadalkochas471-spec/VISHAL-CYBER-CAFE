package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CafeSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CafeSettingsDao {
    @Query("SELECT * FROM cafe_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<CafeSettingsEntity?>

    @Query("SELECT * FROM cafe_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSnapshot(): CafeSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: CafeSettingsEntity)

    @Update
    suspend fun updateSettings(settings: CafeSettingsEntity)
}
