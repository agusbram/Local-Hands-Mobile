package com.undef.localhandsbrambillafunes.ui.components

import android.location.Address
import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellerCreationStatus
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Composable responsable de gestionar el flujo completo
 * de conversión de un usuario a vendedor.
 *
 * Este flujo incluye:
 * - Verificación del estado actual del usuario
 * - Ingreso del nombre del emprendimiento
 * - Selección opcional de ubicación integrada en el mapa (sin navegar)
 * - Navegación automática a la pantalla de ventas
 *
 * @param navController Controlador de navegación.
 * @param sellViewModel ViewModel que maneja el estado de creación del vendedor.
 * @param onDismiss Callback para cerrar el flujo.
 */
@Composable
fun SellerConversionHandler(
    navController: NavController,
    sellViewModel: SellViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val status by sellViewModel.status.collectAsState()
    val entrepreneurshipName by sellViewModel.entrepreneurshipName.collectAsState()
    val address by sellViewModel.address.collectAsState()
    val latitude by sellViewModel.latitude.collectAsState()
    val longitude by sellViewModel.longitude.collectAsState()

    // Estados locales para el control del flujo
    var showEntrepreneurDialog by remember { mutableStateOf(false) }
    var showMapEditor by remember { mutableStateOf(false) }
    
    // Estado para el mapa
    var selectedMapLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedMapAddress by remember { mutableStateOf<String?>(null) }
    var mapSearchQuery by remember { mutableStateOf("") }
    var isGeocoding by remember { mutableStateOf(false) }

    /**
     * Observa los cambios de estado del proceso de creación
     * de vendedor y reacciona en consecuencia.
     */
    LaunchedEffect(status) {
        when (status) {
            SellerCreationStatus.SUCCESS, SellerCreationStatus.ALREADY_EXISTS -> {
                navController.navigate(AppScreens.SellScreen.route)
                onDismiss()
            }
            SellerCreationStatus.IDLE -> {
                // Mostrar directamente el formulario
                showEntrepreneurDialog = true
            }
            SellerCreationStatus.ERROR -> {
                onDismiss()
            }
            else -> {}
        }
    }

    /**
     * Verifica el estado del usuario al iniciar el flujo.
     */
    LaunchedEffect(Unit) {
        sellViewModel.resetConversionState()
        val isAlreadySeller = sellViewModel.isUserAlreadySeller()
        if (isAlreadySeller) {
            navController.navigate(AppScreens.SellScreen.route)
            onDismiss()
        } else {
            // Pre-cargar con la dirección automática del usuario
            sellViewModel.initializeWithUserAddress(context)
            sellViewModel.checkCurrentUserStatus()
        }
    }

    /**
     * Diálogo principal: Ingreso del nombre del emprendimiento
     * y confirmación de ubicación (sin navegar)
     */
    if (showEntrepreneurDialog) {
        val locationText = address ?: "Cargando ubicación..."

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Nuevo Emprendimiento") },
            text = {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)) {
                    Text("Ingrese los datos de su emprendimiento:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo: Nombre del Emprendimiento
                    OutlinedTextField(
                        value = entrepreneurshipName,
                        onValueChange = { sellViewModel.onEntrepreneurshipNameChange(it) },
                        label = { Text("Nombre del emprendimiento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Campo: Ubicación (puede ser editada desde el mapa)
                    OutlinedTextField(
                        value = locationText,
                        onValueChange = {}, 
                        label = { Text("Ubicación") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Botón: Abrir Selector de Ubicación (sin navegar)
                    Button(
                        onClick = { 
                            showMapEditor = true
                            selectedMapLocation = LatLng(latitude, longitude)
                            selectedMapAddress = address
                            mapSearchQuery = address ?: ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar Ubicación en Mapa")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { sellViewModel.convertUserToSeller() },
                    enabled = entrepreneurshipName.isNotBlank() && !address.isNullOrBlank()
                ) { Text("Crear") }
            },
            dismissButton = { 
                TextButton(onClick = { onDismiss() }) { Text("Cancelar") } 
            }
        )
    }

    /**
     * Diálogo del Mapa: Seleccionar ubicación directamente
     * integrado sin navegar a otra pantalla
     */
    if (showMapEditor) {
        InlineMapSelector(
            initialAddress = address ?: "",
            currentLatitude = selectedMapLocation?.latitude ?: latitude,
            currentLongitude = selectedMapLocation?.longitude ?: longitude,
            context = context,
            onLocationSelected = { selectedAddress, selectedLat, selectedLon ->
                sellViewModel.onLocationChange(selectedAddress)
                sellViewModel.onCoordinatesChange(selectedLat, selectedLon)
                showMapEditor = false
                showEntrepreneurDialog = true
            },
            onDismiss = { 
                showMapEditor = false
                showEntrepreneurDialog = true
            }
        )
    }

    /**
     * Diálogo de carga mostrado durante el procesamiento
     */
    if (status == SellerCreationStatus.LOADING) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Procesando...") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = {}
        )
    }
}

/**
 * Composable que muestra un selector de ubicación integrado usando Google Maps
 * directamente dentro de un AlertDialog, evitando problemas de navegación.
 */
@Composable
private fun InlineMapSelector(
    initialAddress: String,
    currentLatitude: Double,
    currentLongitude: Double,
    context: android.content.Context,
    onLocationSelected: (address: String, latitude: Double, longitude: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    var selectedLocation by remember { mutableStateOf<LatLng?>(LatLng(currentLatitude, currentLongitude)) }
    var selectedAddress by remember { mutableStateOf(initialAddress) }
    var searchQuery by remember { mutableStateOf(initialAddress) }
    var isGeocoding by remember { mutableStateOf(false) }
    
    val defaultLocation = LatLng(-31.4201, -64.1888) // Córdoba
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation ?: defaultLocation, 15f)
    }

    /**
     * Convierte coordenadas a dirección mediante geocodificación inversa.
     * Ejecuta la operación en background con Geocoder.
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
                    selectedAddress = "${address.thoroughfare ?: ""}, ${address.locality ?: ""}"
                    searchQuery = selectedAddress
                }
            } catch (e: IOException) {
                // Error silencioso en geocodificación
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
                    selectedAddress = "${address.thoroughfare ?: ""}, ${address.locality ?: ""}"
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            } catch (e: IOException) {
                // Error silencioso en búsqueda
            }
            isGeocoding = false
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Selecciona ubicación en el mapa") },
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
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )
}