package com.absapp.control.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Норма расхода, т/1000 м², на каждый 1 см толщины покрытия. */
    val normPerCm: Double,
    /** Справочно: норма расхода при толщине 4 см (как в оригинальной таблице). */
    val normPer4cm: Double? = null
)
