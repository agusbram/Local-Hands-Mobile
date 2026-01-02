package com.undef.localhandsbrambillafunes.ui.screens.productdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shop
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.data.model.FavoriteProducts
import com.undef.localhandsbrambillafunes.ui.viewmodel.favorites.FavoriteViewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel


/**
 * Pantalla de detalles del producto que muestra información completa del producto
 * seleccionado, incluyendo imágenes, descripción y opciones de contacto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    product: Product,
    favoriteViewModel: FavoriteViewModel = hiltViewModel<FavoriteViewModel>()
) {

    // Estado para manejar la lista de imágenes del producto
    val productImages = remember { product.images }

    // Control del visor de imágenes
    val pagerState = rememberPagerState(pageCount = { productImages.size })

    /*Cargamos la lista de productos favoritos actuales en la UI*/
    LaunchedEffect(Unit) {
        favoriteViewModel.loadFavorites()
    }
    val favorites by favoriteViewModel.favorites.collectAsState()



    // Estado para el favorito del producto de la base de datos
    val isFavorite = favorites.any { it.id == product.id }

    Scaffold(
        // Barra superior con botón de retroceso
        topBar = {
            TopAppBar(
                // Boton para volver a la pantalla anterior
                title = {
                    Row (verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Volver Atras"
                            )
                        }
                    }
                },
                // Colores para la barra superior
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),  // Color de fondo
                    titleContentColor = Color.White,      // Color del texto
                    actionIconContentColor = Color.White  // Color de los iconos de acción
                ),
                actions = {
                    // Botón para ir a Favoritos
                    IconButton(onClick = { navController.navigate(route = AppScreens.SearchBarScreen.route) }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar"
                        )
                    }

                    // Botón para ir a Perfil
                    IconButton(onClick = { navController.navigate(AppScreens.ProfileScreen.route) }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Seccion de Perfil"
                        )
                    }

                    // Botón para ir a Configuración
                    IconButton(onClick = { navController.navigate(route = AppScreens.SettingsScreen.route) }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Seccion de Settings"
                        )
                    }
                }
            )
        },

        // Implementacion para Material3:
        // Barra inferior con navegacion principal
        bottomBar = {
            // Navegacion inferior con iconos
            NavigationBar(
                containerColor = Color(0xFF242424),
                contentColor = Color.White
            ) {

                // Esquema de color para los diferentes estados de los botones
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,      // Ícono seleccionado
                    unselectedIconColor = Color.White,     // Ícono no seleccionado
                    selectedTextColor = Color.White,      // Texto seleccionado
                    unselectedTextColor = Color.White,      // Texto no seleccionado
                    indicatorColor = Color.Transparent     // Quitar el recuadro
                )

                // Boton de Home o inicio
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(route = AppScreens.HomeScreen.route) }
                )
                // Boton de Favoritos
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(AppScreens.FavoritesScreen.route)}
                )
                // Boton para vender
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(AppScreens.SellScreen.route)}
                )
                // Boton de Categorias
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Categorias") },
                    label = { Text("Categorias")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(AppScreens.CategoryScreen.route) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                // Visor de imágenes con paginación horizontal
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    // Imagen principal del producto
                    AsyncImage(
                        model = productImages[page],
                        contentDescription = "Imagen del producto ${page + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Botón flotante de favorito
                IconButton(
                    onClick = {
                        if (isFavorite) {
                            // Busca la instancia Favorite correcta en la lista
                            val fav = favorites.find { it.id == product.id }
                            fav?.let { favoriteViewModel.removeFavoriteByProductId(product.id) }
                            FavoriteProducts.removeToFavorite(product.id)
                        } else {
                            FavoriteProducts.addToFavorite(product)
                            favoriteViewModel.addFavorite(product.id)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = if (isFavorite) Color.Red else Color.Gray
//                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
//                        contentDescription = "Marcar como favorito",
//                        modifier = Modifier
//                            .size(42.dp),
//                        tint = if (isFavorite) Color(0xFF9370DB) else Color.Gray

                    )
                }

                // Indicadores de página (puntos)
                if (productImages.size > 1) { // Solo se ejecuta si el producto tiene mas de una imagen
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Iterar por cada imagen en la lista 'productImages'
                        repeat(productImages.size) { index ->
                            // Por cada imagen crear un Box: punto indicador
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        // Si el punto corresponde a la imagen actual lo pinta de blanco, de lo contrario, semitransparente
                                        if (pagerState.currentPage == index) Color.White
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            }

            // Contenido informativo
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Nombre y precio
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Ubicacion: ${product.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = "Precio: $${product.price}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

//                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Información adicional del producto
                Spacer(modifier = Modifier.height(16.dp))

                // Información adicional del producto
                Spacer(modifier = Modifier.height(16.dp))

                // Categoría
                Text(
                    text = "Categoría: ${product.category}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Productor
                Text(
                    text = "Vendedor: ${product.producer}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de contacto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón de Email
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* TODO: Implementar acción */ }
                    ) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Botón de Teléfono
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* TODO: Implementar acción */ }
                    ) {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = "Teléfono",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Botón de Compartir
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* TODO: Implementar acción */ }
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Compartir",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}