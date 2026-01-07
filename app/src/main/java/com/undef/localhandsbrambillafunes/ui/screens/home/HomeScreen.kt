package com.undef.localhandsbrambillafunes.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.undef.localhandsbrambillafunes.data.model.ProductListItem
import com.undef.localhandsbrambillafunes.ui.viewmodel.products.ProductViewModel
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.setValue
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellerCreationStatus

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
fun HomeScreen(navController: NavController,
               productViewModel: ProductViewModel = hiltViewModel<ProductViewModel>(),
               sellViewModel: SellViewModel = hiltViewModel<SellViewModel>()
) {
    // Estado reactivo que contiene la lista de productos
    val products by productViewModel.products.collectAsState()

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
                    label = { Text("Inicio")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { /* Implementar navegación a Home */ }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(route = AppScreens.FavoritesScreen.route) }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender")},
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { showSellDialog = true }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Categorias") },
                    label = { Text("Categorías")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = {  }
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
            // Encabezado de sección
            item {
                Text(
                    text = "Productos Destacados",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Lista de productos
            items(
                items = products,
                key = { it.id } // Clave única para cada producto
            ) { product ->
                ProductListItem(product = product, navController = navController)
            }
        }
    }

    /**
     * Flujo de navegación hacia la pantalla de venta.
     */
    if (showSellDialog) {
        HandleNavigationToSellScreen(
            navController = navController,
            sellViewModel = sellViewModel,
            onDismiss = { showSellDialog = false }
        )
    }

}

/**
 * Composable responsable de gestionar el flujo completo
 * de conversión de un usuario a vendedor.
 *
 * Este flujo incluye:
 * - Verificación del estado actual del usuario
 * - Confirmación de intención
 * - Ingreso del nombre del emprendimiento
 * - Navegación automática a la pantalla de ventas
 *
 * @param navController Controlador de navegación.
 * @param sellViewModel ViewModel que maneja el estado de creación del vendedor.
 * @param onDismiss Callback para cerrar el flujo.
 */
@Composable
fun HandleNavigationToSellScreen(
    navController: NavController,
    sellViewModel: SellViewModel,
    onDismiss: () -> Unit
) {
    val status by sellViewModel.status.collectAsState()
    val entrepreneurshipName by sellViewModel.entrepreneurshipName.collectAsState()


    // Estados locales para el control de diálogos
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showEntrepreneurDialog by remember { mutableStateOf(false) }

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
                // El ViewModel ha confirmado que el usuario no es vendedor. Mostramos el diálogo.
                showConfirmationDialog = true
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
        // Verificación rápida inicial
        val isAlreadySeller = sellViewModel.isUserAlreadySeller()

        if (isAlreadySeller) {
            // Navegar directamente
            navController.navigate(AppScreens.SellScreen.route)
            onDismiss()
        } else {
            // Solo si NO es vendedor, iniciar el flujo de verificación
            sellViewModel.checkCurrentUserStatus()
        }
    }

    /**
     * Diálogo de confirmación para convertirse en emprendedor.
     */
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text("Convertirse en emprendedor") },
            text = { Text("Usted está a punto de convertirse en emprendedor. ¿Está seguro?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmationDialog = false
                    showEntrepreneurDialog = true
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text("No")
                }
            }
        )
    }

    /**
     * Diálogo para ingresar el nombre del emprendimiento.
     */
    if (showEntrepreneurDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text("Nombre del emprendimiento") },
            text = {
                Column {
                    Text("Ingrese el nombre de su emprendimiento:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = entrepreneurshipName,
                        onValueChange = { sellViewModel.onEntrepreneurshipNameChange(it) },
                        placeholder = { Text("Ej: Dulzuras del Valle") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    sellViewModel.convertUserToSeller()
                },
                    enabled = entrepreneurshipName.isNotBlank()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    /**
     * Diálogo de carga mostrado durante el procesamiento.
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
