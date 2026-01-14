/**
 * Paquete que contiene los componentes de la UI para la pantalla principal de la aplicación.
 */
package com.undef.localhandsbrambillafunes.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.data.model.ProductListItem
import com.undef.localhandsbrambillafunes.ui.components.SellerConversionHandler
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.products.ProductViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel

/**
 * Composable que implementa la pantalla de búsqueda de productos.
 *
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas.
 * @param productViewModel Modelo de vista para gestionar la lógica de la pantalla de búsqueda.
 */
@Composable
fun SearchBarScreen(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel<ProductViewModel>()
) {
    // Estado para manejar el diálogo de convertirse en vendedor
    var showSellDialog by remember { mutableStateOf(false) }

    // Obtenemos el estado de búsqueda y los resultados del ViewModel
    val searchQuery by productViewModel.searchQuery.collectAsState()
    val filteredProducts by productViewModel.searchResults.collectAsState()

    Scaffold(
        // Barra inferior con navegación principal
        bottomBar = {
            // Navegación inferior con iconos
            NavigationBar(
                containerColor = Color(0xFF242424), // Color de fondo de la barra
                contentColor = Color.White          // Color del contenido
            ) {
                // Esquema de color para los diferentes estados de los botones
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,      // Ícono seleccionado
                    unselectedIconColor = Color.White,    // Ícono no seleccionado
                    selectedTextColor = Color.White,      // Texto seleccionado
                    unselectedTextColor = Color.White,    // Texto no seleccionado
                    indicatorColor = Color.Transparent    // Quitar el recuadro de indicador
                )

                // Botón de Home o inicio
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(route = AppScreens.HomeScreen.route) }
                )

                // Botón de Favoritos
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(route = AppScreens.FavoritesScreen.route) }
                )

                // Botón para vender
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { showSellDialog = true }
                )

                // Botón de Categorías
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Categorias") },
                    label = { Text("Categorias")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(route = AppScreens.CategoryScreen.route) }
                )
            }
        }
    ) { paddingValues ->
        /* Contenido Principal */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica el padding necesario para evitar superposición con la barra inferior
        ) {
            // Fila superior con botón de retroceso y barra de búsqueda
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp)
            ) {
                // Botón para volver atrás
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Volver Atrás"
                    )
                }

                // Componente de barra de búsqueda personalizado
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        // Actualiza la consulta y filtra los productos en tiempo real
                        productViewModel.onSearchQueryChanged(newQuery)
                    },
                    modifier = Modifier.fillMaxWidth() // La barra de búsqueda ocupa todo el ancho disponible
                )
            }

            // Indicador pequeño que indica que se está buscando algún producto en el buscador
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "Resultados para: '$searchQuery'",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Gray
                )
            }

            // Espacio vertical entre la barra de búsqueda y la lista de resultados
            Spacer(modifier = Modifier.height(16.dp))

            // Lista de resultados de búsqueda en caso que no se encuentre ningún producto en el buscador
            LazyColumn {
                if (filteredProducts.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Text(
                            "No se encontraron productos, categorías o vendedores",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(
                    items = filteredProducts,
                    key = { it.id }
                ) { product ->
                    ProductListItem(product = product, navController = navController)
                }
            }
        }
    }
    // Muestra el diálogo de conversión a vendedor
    if (showSellDialog) {
        SellerConversionHandler(
            navController = navController,
            sellViewModel = hiltViewModel<SellViewModel>(),
            onDismiss = { showSellDialog = false }
        )
    }
}

/**
 * Componente personalizado para la barra de búsqueda.
 *
 * @param query Texto actual de la consulta de búsqueda.
 * @param onQueryChange Función de callback que se ejecuta cuando cambia el texto de búsqueda.
 * @param modifier Modificador opcional para personalizar la apariencia y el comportamiento.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,                      // Valor actual del campo de texto
        onValueChange = onQueryChange,      // Callback cuando cambia el texto
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") }, // Icono de búsqueda
        placeholder = { Text("Buscar productos") }, // Texto de placeholder
        singleLine = true,                  // Limita a una sola línea
        shape = RoundedCornerShape(16.dp),  // Bordes redondeados para el campo
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text, // Teclado de texto estándar
            imeAction = androidx.compose.ui.text.input.ImeAction.Search,    // Cambia el botón "Enter" por una lupa
            autoCorrect = true // Habilita la autocorrección y facilita las tildes
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,       // Color de fondo cuando tiene foco
            unfocusedContainerColor = Color.Transparent,     // Color de fondo cuando no tiene foco
            focusedIndicatorColor = MaterialTheme.colorScheme.primary, // Color del borde con foco
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant // Color del borde sin foco
        ),
        modifier = modifier // Aplica el modificador recibido como parámetro
    )
}