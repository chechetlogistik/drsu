package com.absapp.control.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GpsState(
    val isTracking: Boolean = false,
    val startLocation: Location? = null,
    val currentLocation: Location? = null,
    val distanceM: Double = 0.0,
    val accuracyM: Float? = null,
    val error: String? = null
)

/**
 * Использует стандартный LocationManager (без Google Play Services и без сторонних
 * SDK) — работает на любом Android-телефоне и не требует интернета для самого
 * измерения расстояния. Дистанция считается по прямой от точки старта (нажатие
 * "Старт") до текущей позиции — это соответствует ходьбе вдоль укладываемой полосы.
 */
class GpsTracker(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _state = MutableStateFlow(GpsState())
    val state: StateFlow<GpsState> = _state

    private var listener: LocationListener? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (_state.value.isTracking) return

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) {
            _state.value = _state.value.copy(error = "GPS выключен. Включите геолокацию в настройках телефона.")
            return
        }

        val newListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val start = _state.value.startLocation ?: location
                val dist = start.distanceTo(location).toDouble()
                _state.value = _state.value.copy(
                    startLocation = start,
                    currentLocation = location,
                    distanceM = dist,
                    accuracyM = location.accuracy,
                    error = null
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                _state.value = _state.value.copy(error = "GPS выключен во время отслеживания.")
            }
        }
        listener = newListener

        try {
            locationManager.requestLocationUpdates(provider, 1000L, 0.5f, newListener)
            val last = locationManager.getLastKnownLocation(provider)
            val useLastAsStart = last != null && (System.currentTimeMillis() - last.time) < 30_000
            _state.value = GpsState(
                isTracking = true,
                startLocation = if (useLastAsStart) last else null,
                currentLocation = last,
                distanceM = 0.0,
                accuracyM = last?.accuracy
            )
        } catch (e: SecurityException) {
            _state.value = _state.value.copy(error = "Нет разрешения на геолокацию.")
        }
    }

    fun stop() {
        listener?.let { locationManager.removeUpdates(it) }
        listener = null
        _state.value = _state.value.copy(isTracking = false)
    }

    fun reset() {
        stop()
        _state.value = GpsState()
    }
}
