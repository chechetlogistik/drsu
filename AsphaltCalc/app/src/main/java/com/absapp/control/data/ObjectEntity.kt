package com.absapp.control.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Объект (участок укладки). Материал и его норма сохраняются "снимком" на момент
 * создания/редактирования объекта — это защищает уже созданные объекты от неожиданных
 * изменений, если позже кто-то поправит значение в справочнике материалов.
 */
@Entity(tableName = "objects")
data class ObjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val materialId: Long?,
    val materialName: String,
    val normPerCm: Double,
    val thicknessCm: Double,
    val widthM: Double,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
