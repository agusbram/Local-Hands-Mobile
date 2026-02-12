package com.undef.localhandsbrambillafunes.ui.screens.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultLocation = LatLng(-31.4201, -64.1888) // Córdoba, Argentina
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
    }

    fun getAddressFromLatLng(latLng: LatLng) {
        coroutineScope.launch {
            isLoading = true
            try {
                val geocoder = Geocoder(context)
                val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    selectedAddress = address.thoroughfare + ", " + address.locality
                    searchQuery = selectedAddress ?: ""
                }
            } catch (e: IOException) {
                // Handle error
            }
            isLoading = false
        }
    }

    fun searchLocation() {
        coroutineScope.launch {
            isLoading = true
            try {
                val geocoder = Geocoder(context)
                val addresses: List<Address>? = geocoder.getFromLocationName(searchQuery, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    selectedLocation = latLng
                    selectedAddress = address.thoroughfare + ", " + address.locality
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            } catch (e: IOException) {
                // Handle error
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona tu ubicación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedAddress != null && selectedLocation != null) {
                FloatingActionButton(onClick = {
                    Log.d("LocationPickerScreen", "Confirmando ubicación: $selectedAddress (Lat: ${selectedLocation?.latitude}, Lon: ${selectedLocation?.longitude})")
                    val previousEntry = navController.previousBackStackEntry
                    if (previousEntry != null) {
                        previousEntry.savedStateHandle.set("picked_location_address", selectedAddress)
                        previousEntry.savedStateHandle.set("picked_location_latitude", selectedLocation?.latitude ?: 0.0)
                        previousEntry.savedStateHandle.set("picked_location_longitude", selectedLocation?.longitude ?: 0.0)
                        Log.d("LocationPickerScreen", "✅ Ubicación y coordenadas guardadas en savedStateHandle")
                    } else {
                        Log.e("LocationPickerScreen", "❌ previousBackStackEntry es null, no se pudo guardar la ubicación")
                    }
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar ubicación")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar dirección") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    trailingIcon = {
                        IconButton(onClick = { searchLocation() }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                        getAddressFromLatLng(latLng)
                    }
                ) {
                    selectedLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = selectedAddress ?: "Ubicación seleccionada"
                        )
                    }
                }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
