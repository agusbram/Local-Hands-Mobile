package com.undef.localhandsbrambillafunes.ui.components

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Composable reutilizable que permite seleccionar una ubicación usando Google Maps.
 * 
 * Se presenta como un AlertDialog que contiene:
 * - Buscador de direcciones
 * - Google Maps interactivo
 * - Geocodificación automática
 * 
 * @param title Título del diálogo
 * @param initialAddress Dirección inicial (pre-cargada)
 * @param initialLatitude Latitud inicial
 * @param initialLongitude Longitud inicial
 * @param context Contexto de aplicación para geocodificación
 * @param onLocationSelected Callback con (dirección, latitud, longitud)
 * @param onDismiss Callback cuando se cancela
 * @param confirmButtonText Texto del botón de confirmación
 */
@Composable
fun LocationMapSelector(
    title: String = "Selecciona tu ubicación",
    initialAddress: String = "",
    initialLatitude: Double = -31.4201, // Córdoba
    initialLongitude: Double = -64.1888,
    context: Context,
    onLocationSelected: (address: String, latitude: Double, longitude: Double) -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Confirmar"
) {
    val coroutineScope = rememberCoroutineScope()
    
    var selectedLocation by remember { mutableStateOf<LatLng?>(LatLng(initialLatitude, initialLongitude)) }
    var selectedAddress by remember { mutableStateOf(initialAddress) }
    var searchQuery by remember { mutableStateOf(initialAddress) }
    var isGeocoding by remember { mutableStateOf(false) }
    
    val defaultLocation = LatLng(-31.4201, -64.1888) // Córdoba
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: defaultLocation, 15f)
    }

    /**
     * Convierte coordenadas a dirección mediante geocodificación inversa.
     * Ejecuta la operación en background y actualiza el estado con la dirección encontrada.
     *
     * @param latLng Coordenadas a geocodificar
     */
    fun getAddressFromLatLng(latLng: LatLng) {
        coroutineScope.launch {
            isGeocoding = true
            try {
                val geocoder = Geocoder(context)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    selectedAddress = buildAddressString(address)
                    searchQuery = selectedAddress
                }
            } catch (e: IOException) {
                // Error silencioso
            }
            isGeocoding = false
        }
    }

    /**
     * Busca una dirección por nombre usando Geocoder.
     * Mueve el mapa a los resultados encontrados.
     */
    fun searchLocation() {
        coroutineScope.launch {
            isGeocoding = true
            try {
                val geocoder = Geocoder(context)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(searchQuery, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    selectedLocation = latLng
                    selectedAddress = buildAddressString(address)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            } catch (e: IOException) {
                // Error silencioso
            }
            isGeocoding = false
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { searchLocation() }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Google Maps
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                        getAddressFromLatLng(latLng)
                    }
                ) {
                    selectedLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = selectedAddress
                        )
                    }
                }
                
                if (isGeocoding) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Muestra la dirección seleccionada
                OutlinedTextField(
                    value = selectedAddress,
                    onValueChange = {},
                    label = { Text("Dirección seleccionada") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (selectedLocation != null && selectedAddress.isNotEmpty()) {
                        onLocationSelected(
                            selectedAddress,
                            selectedLocation!!.latitude,
                            selectedLocation!!.longitude
                        )
                    }
                }
            ) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )
}

/**
 * Función auxiliar para construir una dirección legible desde un objeto Address
 */
private fun buildAddressString(address: Address): String {
    val parts = mutableListOf<String>()
    
    address.thoroughfare?.let { parts.add(it) }
    address.locality?.let { parts.add(it) }
    address.adminArea?.let { parts.add(it) }
    
    return parts.joinToString(", ")
}
