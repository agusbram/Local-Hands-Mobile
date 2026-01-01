package com.undef.localhandsbrambillafunes.ui.screens.settings


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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController,
                   settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>()
) {
    // Se lee la ciudad seleccionada por el usuario en tiempo real a través del ViewModel
    val selectedCity by settingsViewModel.userLocation.collectAsState()
    //var selectedCity by remember { mutableStateOf("Rosario, Santa Fe") }
    var selectedFrequency by remember { mutableStateOf("Una vez al día") }

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
                    // Botón para ir a Perfil
                    IconButton(onClick = { navController.navigate(route = AppScreens.ProfileScreen.route) }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Seccion de Perfil"
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
                    onClick = { /* TODO: Implementar navegacion */ }
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
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Text("Preferencias de búsqueda", style = MaterialTheme.typography.titleMedium)

                FavoriteCategoriesSection()

                /*Desplegable para seleccionar la ubicacion por defecto*/
                DropdownUbication (
                    selectedCity = selectedCity,
                    onCitySelected = { newCity ->
                        settingsViewModel.updateUserLocation(newCity)
                    }
                )

                Spacer(Modifier.height(16.dp))

                DropdownNotificationFrequency(selectedFrequency = selectedFrequency,
                    onFrequencySelected = { selectedFrequency = it }
                )

                Text("Alertas de favoritos:")
                AlertsSwitchFavorites()

                Spacer(Modifier.height(16.dp))

                Text("Soporte", style = MaterialTheme.typography.titleMedium)
                Text("¿Consultas? Envíanos un email:")
                Text("soporte@manoslocales.app", color = Color.Blue)
            }
        }
    }
}

@Composable
fun FavoriteCategoriesSection() {
    // Estado de cada categoría para poder hacer interactivo cada chip botón
    var foodSelected by remember { mutableStateOf(true) }
    var textilesSelected by remember { mutableStateOf(true) }
    var craftsSelected by remember { mutableStateOf(false) }
    var cosmeticsSelected by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally //Centramos horizontalmente todo el contenido de las categorias favoritas
    ) {
        Text("Categorías favoritas", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center) { //Forzamos el centrado de los chips interactivos horizontalmente
            CategoryChip("Alimentos", foodSelected) { foodSelected = !foodSelected } //Para hacer que sean interactivos utilizamos la funcion lambda
            CategoryChip("Textiles", textilesSelected) { textilesSelected = !textilesSelected }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CategoryChip("Artesanías", craftsSelected) { craftsSelected = !craftsSelected }
            CategoryChip("Cosmética", cosmeticsSelected) { cosmeticsSelected = !cosmeticsSelected }
        }
    }
}

@Composable
fun CategoryChip(text: String, selected: Boolean, onToggle: () -> Unit) {
    androidx.compose.material3.Surface (
        color = if (selected) Color(0xFF81C784) else Color.Gray, //Cambia el color del chip que fue seleccionado
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(end = 8.dp).clickable { onToggle() }
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}


@Composable
fun DropdownUbication (
    selectedCity: String,
    onCitySelected: (String) -> Unit
) {
    val cities = listOf("Rosario, Santa Fe", "Córdoba, Córdoba", "Mendoza, Mendoza", "Buenos Aires, CABA")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {

        Text(text = "Ubicación por defecto", style = MaterialTheme.typography.titleMedium)

        Box {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = { }, //No necesitamos modificar la ubicación, por el momento es estática
                readOnly = true, //No editable, pero visible y clickable -->  En este caso se selecciona una ciudad desde una lista, no se tipea el texto
                label = { Text("Seleccionar ciudad") },
                modifier = Modifier.fillMaxWidth(),
                /*Definimos el icono de flecha hacia abajo para el desplegable*/
                trailingIcon = {
                    IconButton (onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expandir lista"
                        )
                    }
                }
            )
            /*Menú desplegable (DropdownMenu) que aparece cuando el usuario toca el icono de flecha en el OutlinedTextField*/
            DropdownMenu (
                expanded = expanded, //Controla si el menú esta abierto o cerrado
                onDismissRequest = { expanded = false }, //Se ejecuta cuando se hace clic fuera del menú, cerrándolo
                modifier = Modifier.fillMaxWidth()
            ) {
                /*Recorre la lista de ciudades para mostrar cada una como opción*/
                cities.forEach { city ->
                    DropdownMenuItem( //Cada ítem clickable en el menú (cada ciudad)
                        text = { Text(city) },
                        /*Al hacer clic en una ciudad:
                        1. La pasa al padre mediante onCitySelected
                        2. Cierra el menú (expanded = false)*/
                        onClick = {
                            onCitySelected(city)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/*Misma lógica que la función DropdownUbication (la función creada arriba)*/
@Composable
fun DropdownNotificationFrequency(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit
) {
    val frequencies = listOf("Cada hora", "Cada 6 horas", "Cada 12 horas", "Una vez al día")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Frecuencia de notificaciones",
            style = MaterialTheme.typography.titleMedium
        )

        Box {
            OutlinedTextField(
                value = selectedFrequency,
                onValueChange = { }, // No editable manualmente
                readOnly = true,
                label = { Text("Seleccionar frecuencia") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expandir lista"
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                frequencies.forEach { frequency ->
                    DropdownMenuItem(
                        text = { Text(frequency) },
                        onClick = {
                            onFrequencySelected(frequency)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertsSwitchFavorites() {
    var checked by remember { mutableStateOf(true) }

    androidx.compose.material3.Switch(
        checked = checked, //Estado inicial del interruptor
        onCheckedChange = { //Es una devolución de llamada a la que se llama cuando cambia el estado del interruptor
            checked = it
        },
        thumbContent = if(checked) { //Para personalizar la apariencia del pulgar cuando está marcado.
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
    )
}


