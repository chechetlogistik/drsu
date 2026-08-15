package com.absapp.control.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class Repository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val materialDao = db.materialDao()
    private val objectDao = db.objectDao()
    private val logDao = db.logEntryDao()

    val materials: Flow<List<MaterialEntity>> = materialDao.getAll()
    val objects: Flow<List<ObjectEntity>> = objectDao.getAll()
    val activeObject: Flow<ObjectEntity?> = objectDao.getActive()
    val logEntries: Flow<List<LogEntryEntity>> = logDao.getAll()

    suspend fun seedIfEmpty() {
        if (materialDao.count() == 0) {
            SeedData.initialMaterials.forEach { materialDao.insert(it) }
        }
    }

    suspend fun addMaterial(m: MaterialEntity) = materialDao.insert(m)
    suspend fun updateMaterial(m: MaterialEntity) = materialDao.update(m)
    suspend fun deleteMaterial(m: MaterialEntity) = materialDao.delete(m)

    suspend fun addObject(o: ObjectEntity): Long {
        val id = objectDao.insert(o)
        objectDao.setActive(id)
        return id
    }
    suspend fun updateObject(o: ObjectEntity) = objectDao.update(o)
    suspend fun deleteObject(o: ObjectEntity) = objectDao.delete(o)
    suspend fun setActiveObject(id: Long) = objectDao.setActive(id)

    suspend fun addLogEntry(e: LogEntryEntity) = logDao.insert(e)
    suspend fun deleteLogEntry(e: LogEntryEntity) = logDao.delete(e)
}
