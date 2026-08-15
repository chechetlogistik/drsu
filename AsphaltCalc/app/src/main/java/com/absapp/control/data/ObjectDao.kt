package com.absapp.control.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectDao {
    @Query("SELECT * FROM objects ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ObjectEntity>>

    @Query("SELECT * FROM objects WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<ObjectEntity?>

    @Insert
    suspend fun insert(obj: ObjectEntity): Long

    @Update
    suspend fun update(obj: ObjectEntity)

    @Delete
    suspend fun delete(obj: ObjectEntity)

    @Query("UPDATE objects SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE objects SET isActive = 1 WHERE id = :id")
    suspend fun setActiveInternal(id: Long)

    @Transaction
    suspend fun setActive(id: Long) {
        clearActive()
        setActiveInternal(id)
    }
}
