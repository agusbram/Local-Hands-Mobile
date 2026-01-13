package com.undef.localhandsbrambillafunes.ui.screens.entrepreneur

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.ui.viewmodel.products.ProductViewModel
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.undef.localhandsbrambillafunes.ui.viewmodel.session.SessionViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellerCreationStatus

/**
 * Pantalla principal del flujo de venta para emprendedores.
 *
 * Este composable es responsable de:
 * - Obtener la información del usuario autenticado desde [SessionViewModel].
 * - Verificar o crear el perfil de vendedor mediante [SellViewModel].
 * - Cargar los productos asociados al vendedor actual usando [ProductViewModel].
 * - Renderizar la interfaz adecuada según el estado del proceso de creación del vendedor.
 *
 * El comportamiento de la pantalla está controlado por el estado [SellerCreationStatus],
 * mostrando indicadores de carga, mensajes de error o el dashboard del vendedor según corresponda.
 *
 * @param navController Controlador de navegación para gestionar los cambios de pantalla.
 * @param productViewModel ViewModel encargado de la gestión de productos.
 * @param sessionViewModel ViewModel que provee la información de la sesión actual.
 * @param sellViewModel ViewModel responsable de la lógica de creación y validación del vendedor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel<ProductViewModel>(),
    sessionViewModel: SessionViewModel = hiltViewModel<SessionViewModel>(),
    sellViewModel: SellViewModel = hiltViewModel<SellViewModel>()
) {
    /**
     * Estados locales que almacenan la información del usuario actual
     * necesaria para crear o validar el perfil de vendedor.
     */
    val currentUserIdState = remember { mutableStateOf<Int?>(null) }
    val currentSellerEmailState = remember { mutableStateOf<String?>(null) }

    /*Llamamos a una funcion suspend con corrutinas para obtener los datos del usuario actual*/
    LaunchedEffect(Unit) {
        currentUserIdState.value = sessionViewModel.getCurrentUserId()
        currentSellerEmailState.value = sessionViewModel.getCurrentSellerEmail()
    }

    /**
     * Usamos remember para mantener el MISMO Flow entre recomposiciones
     * El bloque solo se recalcula si currentUserIdState.value cambia.
     * Esto soluciona el pasado error de mostrar intermitentemente la lista de productos
     * del vendedor actualmente logueado, ya que utiliza Flow, que es una tubería que
     * escucha constantemente si hay cambios en los productos del vendedor y, si los hay,
     * actualiza la UI, sin tener que andar constanemente actualizando la lista.
     * Remember solucionó el error.
     * */
    val productsOwnerFlow = remember(currentUserIdState.value) {
        currentUserIdState.value?.let { userId ->
            productViewModel.getMyProducts(userId)
        }
    }

    // Ahora nos suscribimos al Flow estable para mostrar fluidamente los productos del vendedor
    val productsOwnerState by productsOwnerFlow?.collectAsState() ?: remember {
        mutableStateOf(emptyList())
    }

    // Observa el estado del sellViewModel
    val status by sellViewModel.status.collectAsState()

    // Este efecto se dispara cuando el usuario acepta convertirse en vendedor.
    // Se lanza solo cuando tenemos los datos del usuario.
    LaunchedEffect(currentSellerEmailState.value) {
        if (currentSellerEmailState.value != null && status == SellerCreationStatus.IDLE) {
            sellViewModel.checkCurrentUserStatus()
        }
    }

    // Renderiza la UI según el estado del proceso de creación
    when (status) {
        SellerCreationStatus.LOADING -> {
            // Muestra un indicador de carga
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Verificando tu perfil de emprendedor...")
                }
            }
        }
        SellerCreationStatus.SUCCESS, SellerCreationStatus.ALREADY_EXISTS -> {
            // Si tuvo éxito o si ya existía, muestra el dashboard de vendedor
            SellContent(
                navController = navController,
                productsOwnerState = productsOwnerState,
            )
        }
        SellerCreationStatus.ERROR -> {
            // Muestra un mensaje de error con opción a reintentar
            ErrorView(onRetry = {
                // Reintenta la operación con los datos del usuario
                if (currentSellerEmailState.value != null) {
                    sellViewModel.checkCurrentUserStatus()
                }
            })
        }
        SellerCreationStatus.IDLE -> {
            // Estado inicial, muestra un loader mientras se obtienen los datos del usuario
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}


/**
 * Composable que representa el dashboard principal del vendedor.
 *
 * Muestra:
 * - Barra superior con acciones de navegación.
 * - Barra inferior de navegación.
 * - Botón para agregar nuevos productos.
 * - Listado de productos en venta pertenecientes al vendedor.
 *
 * @param navController Controlador de navegación.
 * @param productsOwnerState Lista de productos del vendedor actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellContent(
    navController: NavController,
    productsOwnerState: List<Product>
) {
    Scaffold(
        // Barra superior con acciones
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Volver Atras")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Mis productos en venta")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreens.SearchBarScreen.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { navController.navigate(AppScreens.ProfileScreen.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = { navController.navigate(AppScreens.SettingsScreen.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configuración")
                    }
                }
            )
        },

        // Barra inferior
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF242424),
                contentColor = Color.White
            ) {
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(AppScreens.HomeScreen.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(AppScreens.FavoritesScreen.route) }
                )
            }
        }
    ) { paddingValues ->
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(onClick = { navController.navigate(AppScreens.EditProductScreen.createRoute(0)) }) {
                Text("Agregar nuevo producto")
            }

            Spacer(Modifier.height(16.dp))

            // Lista de productos en venta (estilo unificado)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productsOwnerState) { product ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE6E6EC))
                            .clickable {
                                navController.navigate(
                                    AppScreens.ProductOwnerDetailScreen.createRoute(product.id)
                                )
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageUrl = product.images.firstOrNull()
                            // Imagen del producto (desde URL o base de datos)
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrl),
                                contentDescription = "Imagen del producto ${product.name}",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Información del producto
                            Column {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ver detalle",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable reutilizable que muestra un mensaje de error
 * con la posibilidad de reintentar la acción fallida.
 *
 * Se utiliza cuando ocurre un error al crear o validar
 * el perfil de vendedor.
 *
 * @param onRetry Acción ejecutada cuando el usuario presiona el botón de reintento.
 */
@Composable
fun ErrorView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hubo un problema al crear tu perfil.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}