package com.undef.localhandsbrambillafunes.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.R
import com.undef.localhandsbrambillafunes.ui.components.ProductListItem
import com.undef.localhandsbrambillafunes.ui.viewmodel.products.ProductViewModel
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.setValue
import com.undef.localhandsbrambillafunes.ui.components.SellerConversionHandler
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel


/**
 * Pantalla principal de la aplicación.
 *
 * Esta pantalla actúa como punto de entrada principal para el usuario y
 * presenta una interfaz completa compuesta por:
 * - Barra superior (TopAppBar)
 * - Contenido principal con listado de productos
 * - Barra de navegación inferior (NavigationBar)
 *
 * Implementa el patrón Material Design 3 utilizando [Scaffold] como
 * contenedor estructural.
 *
 * @param navController Controlador de navegación para manejar el flujo entre pantallas.
 * @param productViewModel ViewModel encargado de proveer el listado de productos.
 * @param sellViewModel ViewModel encargado de gestionar la lógica de conversión a vendedor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel<ProductViewModel>(),
    sellViewModel: SellViewModel = hiltViewModel<SellViewModel>()
) {
    // Observamos el nuevo estado de la pantalla principal que ya viene agrupado
    val homeState by productViewModel.homeScreenState.collectAsState()

    // Controla la visualización del flujo de conversión a vendedor
    var showSellDialog by remember { mutableStateOf(false) }

    /**
     * Scaffold es el componente base que proporciona la estructura básica de la pantalla
     * con áreas para barra superior, contenido principal y barra inferior.
     */
    Scaffold(
        /**
         * Barra superior de la aplicación.
         *
         * Incluye:
         * - Logo de la aplicación
         * - Título
         * - Accesos directos a búsqueda, perfil y configuración
         */
        topBar = {
            /**
             * TopAppBar proporciona la barra superior con título y acciones.
             * En este caso incluye el logo de la marca y botones de acción.
             */
            TopAppBar(
                // Logo de la Marca
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        /**
                         * Logo de la aplicación con descripción para accesibilidad
                         */
                        Image(
                            painter = painterResource(id = R.drawable.localhandslogo),
                            contentDescription = "Logo principal de la aplicación",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 8.dp),
                        )
                        /**
                         * Título de la aplicación con estilo en negrita
                         */
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                // Colores personalizados para la barra superior
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),  // Color oscuro para fondo
                    titleContentColor = Color.White,      // Texto blanco
                    actionIconContentColor = Color.White  // Iconos blancos
                ),
                // Acciones disponibles en la barra superior
                actions = {
                    /**
                     * Botón de búsqueda que navega a la pantalla de búsqueda
                     */
                    IconButton(onClick = { navController.navigate(route = AppScreens.SearchBarScreen.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    /**
                     * Botón de perfil que navega a la pantalla de perfil
                     */
                    IconButton(onClick = { navController.navigate(route = AppScreens.ProfileScreen.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Sección de Perfil")
                    }
                    /**
                     * Botón de configuración que navega a la pantalla de ajustes
                     */
                    IconButton(onClick = { navController.navigate(route = AppScreens.SettingsScreen.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Sección de Settings")
                    }
                }
            )
        },

        // Barra inferior de navegación
        bottomBar = {
            /**
             * NavigationBar proporciona la barra de navegación inferior con iconos y etiquetas
             */
            NavigationBar(
                containerColor = Color(0xFF242424),  // Fondo oscuro
                contentColor = Color.White           // Texto blanco
            ) {
                /**
                 * Configuración de colores para los elementos de navegación
                 */
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,      // Ícono seleccionado
                    unselectedIconColor = Color.White,     // Ícono no seleccionado
                    selectedTextColor = Color.White,      // Texto seleccionado
                    unselectedTextColor = Color.White,    // Texto no seleccionado
                    indicatorColor = Color.Transparent     // Sin indicador visual
                )

                /**
                 * Elementos de navegación disponibles:
                 * - Inicio (actualmente seleccionado)
                 * - Favoritos
                 * - Vender
                 * - Categorías
                 */
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = navBarItemColors,
                    selected = true, // Correcto: esta es la pantalla de inicio
                    onClick = { /* No hacer nada, ya estamos aquí */ }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(route = AppScreens.FavoritesScreen.route) }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { showSellDialog = true }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Categorias") },
                    label = { Text("Categorías") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(route = AppScreens.CategoryScreen.route) }
                )
            }
        }
    ) { paddingValues ->
        /**
         * Contenido principal de la pantalla.
         *
         * Muestra un listado vertical de productos destacados.
         */
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Renderizamos las categorías favoritas primero
            homeState.favoriteProducts.forEach { (category, products) ->
                // Título de la categoría favorita
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                // Lista de productos para esa categoría
                items(items = products, key = { "fav-" + it.id }) { product ->
                    ProductListItem(product = product, navController = navController)
                }
                // Separador
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }
            }

            // Renderizamos el resto de los productos
            if (homeState.otherProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "Productos Destacados",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                items(items = homeState.otherProducts, key = { "other-" + it.id }) { product ->
                    ProductListItem(product = product, navController = navController)
                }
            }
        }
    }

    /**
     * Flujo de navegación hacia la pantalla de venta.
     */
    if (showSellDialog) {
        SellerConversionHandler(
            navController = navController,
            sellViewModel = sellViewModel,
            onDismiss = { showSellDialog = false }
        )
    }
}