package com.undef.localhandsbrambillafunes.ui.screens.favorites

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.data.model.ProductListItem
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.favorites.FavoriteViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.session.SessionViewModel
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    favoriteViewModel: FavoriteViewModel = hiltViewModel<FavoriteViewModel>()
) {
    /*Cargamos la lista de productos favoritos actuales en la UI a través de corrutinas
    (ya que loadFavorites utiliza StateFlow y no tipos de variables estáticas)*/
    LaunchedEffect(Unit) {
        favoriteViewModel.loadFavorites()
    }
    val favorites by favoriteViewModel.favorites.collectAsState()


    Scaffold(
        // Barra Superior con título y acciones
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
                    // Botón para explorar o buscar
                    IconButton(onClick = { navController.navigate(route = AppScreens.SearchBarScreen.route) }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar"
                        )
                    }
                    // Botón para ir a Perfil
                    IconButton(onClick = { navController.navigate(route = AppScreens.ProfileScreen.route) }) {
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
                // Boton para vender
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender")},
                    colors = navBarItemColors,
                    selected = true,
                    onClick = { navController.navigate(route = AppScreens.SellScreen.route) }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Se aplica el padding del Scaffold
        ) {
            item {
                Text(
                    text = "Productos Favoritos",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(favorites) { favoriteProduct ->
                ProductListItem(product = favoriteProduct, navController = navController)
            }
        }
    }
}