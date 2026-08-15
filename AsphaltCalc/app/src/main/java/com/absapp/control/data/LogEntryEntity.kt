package com.absapp.control.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val objectId: Long?,
    val objectName: String,
    val materialName: String,
    val massT: Double,
    /** Применённая норма т/1000м² (normPerCm × thicknessCm) на момент записи. */
    val normUsed: Double,
    val thicknessCm: Double,
    val widthM: Double,
    /** Расчётное расстояние по формуле. */
    val targetDistanceM: Double,
    /** Фактически пройдено по GPS, если использовалось. */
    val gpsDistanceM: Double? = null
)
