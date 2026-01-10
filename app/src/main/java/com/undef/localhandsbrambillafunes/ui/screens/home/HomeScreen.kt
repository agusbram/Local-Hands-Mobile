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
import com.undef.localhandsbrambillafunes.ui.components.SellerConversionHandler
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel<ProductViewModel>(),
    sellViewModel: SellViewModel = hiltViewModel<SellViewModel>()
) {
    val products by productViewModel.products.collectAsState()

    var showSellDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.localhandslogo),
                            contentDescription = "Logo principal de la aplicación",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 8.dp),
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(route = AppScreens.SearchBarScreen.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { navController.navigate(route = AppScreens.ProfileScreen.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Sección de Perfil")
                    }
                    IconButton(onClick = { navController.navigate(route = AppScreens.SettingsScreen.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Sección de Settings")
                    }
                }
            )
        },
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
                    selected = true, // Correcto: esta es la pantalla de inicio
                    onClick = { /* No hacer nada, ya estamos aquí */ }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    colors = navBarItemColors,
                    selected = false, // CORREGIDO
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
                    selected = false, // CORREGIDO
                    onClick = { navController.navigate(route = AppScreens.CategoryScreen.route) }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            item {
                Text(
                    text = "Productos Destacados",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(
                items = products,
                key = { it.id }
            ) { product ->
                ProductListItem(product = product, navController = navController)
            }
        }
    }

    if (showSellDialog) {
        SellerConversionHandler(
            navController = navController,
            sellViewModel = sellViewModel,
            onDismiss = { showSellDialog = false }
        )
    }
}