package com.undef.localhandsbrambillafunes.ui.screens.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// TODO: Esta es una pantalla básica, la conectaremos al ViewModel en el siguiente paso.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsByCategoryScreen(
    navController: NavController,
    categoryName: String
) {
    Scaffold(
        // TODO: Añadir TopAppBar con el categoryName como título y botón de volver
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(text = "Mostrando productos para: $categoryName")
            }
            // TODO: Aquí irá la lista de productos filtrados
        }
    }
}