package com.undef.localhandsbrambillafunes.ui.screens.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Pantalla de prueba para mostrar un mapa de Google.
 *
 * Utiliza el composable [GoogleMap] de la librería maps-compose.
 * Se centra en una ubicación por defecto (Córdoba, Argentina) para verificar la configuración.
 */
@Composable
fun MapScreen() {
    // 1. Define una ubicación inicial para el mapa (ej. Córdoba, Argentina).
    val cordoba = LatLng(-31.4201, -64.1888)

    // 2. Crea y recuerda el estado de la cámara del mapa, estableciendo la posición inicial.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cordoba, 11f) // Un nivel de zoom para ver la ciudad
    }

    // 3. Renderiza el componente del mapa de Google.
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}
