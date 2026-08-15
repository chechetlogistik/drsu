package com.absapp.control.util

import com.absapp.control.data.ObjectEntity

fun parseDecimal(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

/** Итоговая норма объекта, т/1000 м² = норма на 1 см × толщина слоя в см. */
fun objectNorm(obj: ObjectEntity): Double = obj.normPerCm * obj.thicknessCm

/**
 * Расстояние (м) = масса (т) / норма (т/1000м²) × 1000 / ширина (м)
 */
fun calcTargetDistanceM(obj: ObjectEntity?, massT: Double?): Double? {
    val o = obj ?: return null
    val mass = massT ?: return null
    val norm = objectNorm(o)
    if (norm <= 0.0 || o.widthM <= 0.0 || mass <= 0.0) return null
    return mass / norm * 1000.0 / o.widthM
}

/**
 * Фактическая (проектная) норма (т/1000м²) = масса (т) / площадь (м²) × 1000,
 * где площадь = длина × ширина.
 */
fun calcActualNorm(massT: Double?, lengthM: Double?, widthM: Double?): Double? {
    val mass = massT ?: return null
    val length = lengthM ?: return null
    val width = widthM ?: return null
    val area = length * width
    if (area <= 0.0) return null
    return mass / area * 1000.0
}
