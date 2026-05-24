package com.project.store.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * LocationHelper - Utilidad para obtener ubicacion GPS.
 *
 * Usa FusedLocationProviderClient de Google Play Services
 * para obtener coordenadas con alta precision.
 * Incluye geocodificacion inversa en espanol para Colombia.
 *
 * @author Julian
 */
object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { continuation ->
                val client = LocationServices.getFusedLocationProviderClient(context)
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMaxUpdates(1)
                    .build()
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation
                        if (location != null) {
                            continuation.resume(Pair(location.latitude, location.longitude))
                        } else {
                            continuation.resume(null)
                        }
                        client.removeLocationUpdates(this)
                    }
                }
                client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                continuation.invokeOnCancellation {
                    client.removeLocationUpdates(callback)
                }
            }
        }
    }

    fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("es", "CO"))
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    if (!address.thoroughfare.isNullOrEmpty()) append(address.thoroughfare)
                    if (!address.subThoroughfare.isNullOrEmpty()) append(" ${address.subThoroughfare}")
                    if (!address.locality.isNullOrEmpty()) append(", ${address.locality}")
                    if (!address.adminArea.isNullOrEmpty()) append(", ${address.adminArea}")
                }
            } else {
                "Ubicación obtenida (sin dirección)"
            }
        } catch (e: Exception) {
            "Lat: ${"%.4f".format(lat)}, Lng: ${"%.4f".format(lng)}"
        }
    }
}
