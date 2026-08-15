package com.absapp.control.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.absapp.control.data.LogEntryEntity
import com.absapp.control.data.MaterialEntity
import com.absapp.control.data.ObjectEntity
import com.absapp.control.data.Repository
import com.absapp.control.location.GpsState
import com.absapp.control.location.GpsTracker
import com.absapp.control.util.calcTargetDistanceM
import com.absapp.control.util.objectNorm
import com.absapp.control.util.parseDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = Repository(application)
    val gpsTracker = GpsTracker(application)

    init {
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    val materials: StateFlow<List<MaterialEntity>> =
        repo.materials.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val objects: StateFlow<List<ObjectEntity>> =
        repo.objects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeObject: StateFlow<ObjectEntity?> =
        repo.activeObject.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val logEntries: StateFlow<List<LogEntryEntity>> =
        repo.logEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gpsState: StateFlow<GpsState> = gpsTracker.state

    private val _massText = MutableStateFlow("")
    val massText: StateFlow<String> = _massText

    /** Разрешаем вводить только цифры и один разделитель (запятую или точку). */
    fun setMassText(value: String) {
        val cleaned = StringBuilder()
        var separatorUsed = false
        for (c in value) {
            when {
                c.isDigit() -> cleaned.append(c)
                (c == ',' || c == '.') && !separatorUsed -> {
                    cleaned.append('.')
                    separatorUsed = true
                }
            }
        }
        _massText.value = cleaned.toString()
    }

    /** Кнопки-маяки 10/20/30 т: сразу выставляют массу этим значением. */
    fun setMassQuick(t: Int) {
        _massText.value = t.toString()
    }

    fun saveLogEntry(onSaved: () -> Unit = {}) {
        val obj = activeObject.value ?: return
        val mass = parseDecimal(_massText.value) ?: return
        val target = calcTargetDistanceM(obj, mass) ?: return
        val gpsDist = gpsTracker.state.value.distanceM.takeIf { it > 0.0 }

        viewModelScope.launch {
            repo.addLogEntry(
                LogEntryEntity(
                    objectId = obj.id,
                    objectName = obj.name,
                    materialName = obj.materialName,
                    massT = mass,
                    normUsed = objectNorm(obj),
                    thicknessCm = obj.thicknessCm,
                    widthM = obj.widthM,
                    targetDistanceM = target,
                    gpsDistanceM = gpsDist
                )
            )
            _massText.value = ""
            gpsTracker.reset()
            onSaved()
        }
    }

    fun deleteLogEntry(e: LogEntryEntity) = viewModelScope.launch { repo.deleteLogEntry(e) }

    fun addObject(name: String, material: MaterialEntity, thicknessCm: Double, widthM: Double) {
        viewModelScope.launch {
            repo.addObject(
                ObjectEntity(
                    name = name,
                    materialId = material.id,
                    materialName = material.name,
                    normPerCm = material.normPerCm,
                    thicknessCm = thicknessCm,
                    widthM = widthM
                )
            )
        }
    }

    fun updateObject(o: ObjectEntity) = viewModelScope.launch { repo.updateObject(o) }
    fun deleteObject(o: ObjectEntity) = viewModelScope.launch { repo.deleteObject(o) }
    fun setActiveObject(id: Long) = viewModelScope.launch { repo.setActiveObject(id) }

    fun addMaterial(name: String, normPerCm: Double, normPer4cm: Double?) {
        viewModelScope.launch {
            repo.addMaterial(MaterialEntity(name = name, normPerCm = normPerCm, normPer4cm = normPer4cm))
        }
    }
    fun updateMaterial(m: MaterialEntity) = viewModelScope.launch { repo.updateMaterial(m) }
    fun deleteMaterial(m: MaterialEntity) = viewModelScope.launch { repo.deleteMaterial(m) }

    fun startGps() = gpsTracker.start()
    fun stopGps() = gpsTracker.stop()
    fun resetGps() = gpsTracker.reset()
}
