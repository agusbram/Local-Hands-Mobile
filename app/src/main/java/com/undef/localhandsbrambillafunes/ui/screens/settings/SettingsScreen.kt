package com.undef.localhandsbrambillafunes.ui.screens.settings


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.outlined.EditLocation
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.localhandsbrambillafunes.ui.components.SellerConversionHandler
import com.undef.localhandsbrambillafunes.ui.components.LocationMapSelector
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.settings.SettingsViewModel

/**
 * Pantalla de configuración de usuario.
 * 
 * Permite cambiar la ubicación preferida para búsqueda de productos
 * y acceso a opciones de convertirse en vendedor.
 * 
 * @param navController Controlador para navegación
 * @param settingsViewModel ViewModel que gestiona las preferencias del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>()
) {
    // Observa cambios en la ubicación seleccionada en tiempo real
    val selectedCity by settingsViewModel.userLocation.collectAsState()

    // Controla la visibilidad del diálogo para convertirse en vendedor
    var showSellDialog by remember { mutableStateOf(false) }

    // Recibe resultados de ubicación desde otras pantallas
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val pickedLocation = savedStateHandle?.get<String>("picked_location")

    // Actualiza ubicación cuando se selecciona desde otra pantalla
    LaunchedEffect(pickedLocation) {
        if (pickedLocation != null) {
            settingsViewModel.updateUserLocation(pickedLocation)
            savedStateHandle.remove<String>("picked_location")
        }
    }

    Scaffold(
        // Barra superior con navegación y acciones
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Volver Atras"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(route = AppScreens.ProfileScreen.route) }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Seccion de Perfil"
                        )
                    }
                }
            )
        },

        // Barra inferior con navegación principal
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF242424),
                contentColor = Color.White
            ) {

                // Opciones de navegación inferior
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
                    label = { Text("Inicio") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(route = AppScreens.HomeScreen.route) }
                )
                // Boton de Favoritos
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { navController.navigate(AppScreens.FavoritesScreen.route) }
                )
                // Boton para vender
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Shop, contentDescription = "Vender") },
                    label = { Text("Vender") },
                    colors = navBarItemColors,
                    selected = false,
                    onClick = { showSellDialog = true }
                )
                // Boton de Categorias
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Categorias") },
                    label = { Text("Categorias") },
                    colors = navBarItemColors,
                    selected = false,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Text("Preferencias de búsqueda", style = MaterialTheme.typography.titleMedium)

                FavoriteCategoriesSection(settingsViewModel = settingsViewModel)

                /* Ubicación por defecto para la búsqueda de productos */
                LocationSettingItem(
                    selectedCity = selectedCity,
                    context = LocalContext.current,
                    settingsViewModel = settingsViewModel
                )

                Spacer(Modifier.height(16.dp))

                Text("Alertas de favoritos", style = MaterialTheme.typography.titleMedium)
                AlertsSwitchFavorites()

                Spacer(Modifier.height(16.dp))

                /*Sección de soporte al usuario*/
                Text("Soporte", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "¿Consultas? Envíanos un email:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))

                // Email + mensaje informativo persistente
                Column {
                    SupportEmailLink()

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Una vez enviado el correo, nuestro equipo de soporte se pondrá en contacto a la brevedad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
 * Composable que muestra y permite cambiar la ubicación por defecto
 * usando Google Maps integrado.
 *
 * @param selectedCity La ciudad actualmente seleccionada.
 * @param context Contexto para Google Maps.
 * @param settingsViewModel ViewModel para actualizar la ubicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSettingItem(
    selectedCity: String,
    context: android.content.Context,
    settingsViewModel: SettingsViewModel
) {
    var showMapSelector by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {

        Text(text = "Ubicación por defecto", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = { showMapSelector = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.EditLocation,
                contentDescription = "Cambiar ubicación",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(selectedCity.ifEmpty { "Seleccionar ubicación" })
        }
    }

    // Selector de ubicación con Google Maps - abre diálogo para cambiar ubicación preferida
    if (showMapSelector) {
        LocationMapSelector(
            title = "Selecciona tu ubicación por defecto",
            initialAddress = selectedCity,
            context = context,
            onLocationSelected = { selectedAddress, latitude, longitude ->
                settingsViewModel.updateUserLocation(selectedAddress, latitude, longitude)
                showMapSelector = false
            },
            onDismiss = { showMapSelector = false },
            confirmButtonText = "Guardar Ubicación"
        )
    }
}

/**
 * Sección de selección de categorías favoritas.
 * Muestra todas las categorías disponibles y permite al usuario seleccionar sus favoritas.
 *
 * @param settingsViewModel ViewModel que gestiona las categorías favoritas
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavoriteCategoriesSection(settingsViewModel: SettingsViewModel) {
    // Observa todos las categorías y las favoritas seleccionadas en tiempo real
    val allCategories by settingsViewModel.allCategories.collectAsState()
    val favoriteCategories by settingsViewModel.favoriteCategories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Categorías favoritas", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // FlowRow permite que los elementos fluyan a la siguiente línea si no hay espacio
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Creamos un Chip para cada categoría disponible
            allCategories.forEach { categoryName ->
                val isSelected = categoryName in favoriteCategories
                CategoryChip(categoryName, isSelected) {
                    // Al hacer clic, llamamos al ViewModel para que actualice la preferencia
                    settingsViewModel.updateFavoriteCategory(categoryName, !isSelected)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, selected: Boolean, onToggle: () -> Unit) {
    androidx.compose.material3.Surface(
        color = if (selected) Color(0xFF81C784) else Color.Gray,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(4.dp) // Un padding más pequeño para que quepan más chips
            .clickable { onToggle() }
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}


/**
 * Composable que muestra un enlace de correo electrónico para contacto de soporte.
 *
 * El texto del email se presenta como un enlace subrayado y con color primario.
 * Al ser presionado, se abre un selector de aplicaciones de correo instaladas
 * (Gmail) con los siguientes datos preconfigurados:
 *
 * - Destinatario de soporte
 * - Asunto del mensaje
 * - Cuerpo inicial del correo
 *
 * Implementación técnica:
 * - Utiliza AnnotatedString para asociar metadatos (anotaciones) a una porción del texto.
 * - Convierte la posición del toque (Offset) en un índice de texto mediante TextLayoutResult.
 * - Emplea pointerInput y detectTapGestures para capturar interacciones táctiles.
 * - Usa un Intent ACTION_SEND con un selector ACTION_SENDTO (mailto:) para:
 *   - Limitar las aplicaciones disponibles solo a clientes de correo.
 *   - Permitir que el asunto y el cuerpo del mensaje sean respetados.
 */
@Composable
fun SupportEmailLink() {
    val context = LocalContext.current
    val supportEmail = "soporte@manoslocales.app"

    // Crea un texto enriquecido con estilo y metadatos
    // AnnotatedString permite asociar anotaciones invisibles al texto.
    // La anotación "EMAIL" actúa como identificador para saber qué parte del texto fue presionada
    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            // Adjuntamos una anotación al texto del email
            pushStringAnnotation(tag = "EMAIL", annotation = supportEmail)
            append(supportEmail)
            pop()
        }
    }

    // Guarda información sobre cómo se dibujó el texto en pantalla
    // Permite convertir coordenadas táctiles (Offset) en una posición real del texto (índice).
    // Sin esto, no es posible saber qué parte del texto fue tocada.
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Renderiza el texto enriquecido.
    // Captura el resultado del layout cuando el texto se dibuja.
    Text(
        text = annotatedString,
        onTextLayout = { layoutResult ->
            textLayoutResult = layoutResult
        },
        // Intercepta eventos táctiles sobre el texto.
        // Esta técnica permite comportamiento tipo link dentro de un texto.
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                // Usamos el layoutResult para convertir el Offset (coordenadas) a un Int (índice)
                textLayoutResult?.let { layoutResult ->
                    // Convierte la posición física del toque en un índice del string.
                    // Permite correlacionar el toque con una anotación específica.
                    val position = layoutResult.getOffsetForPosition(offset)
                    // Evita disparar acciones si el usuario toca otra parte del texto.
                    // Permite múltiples enlaces si se extiende la lógica.
                    annotatedString.getStringAnnotations(tag = "EMAIL", start = position, end = position)
                        .firstOrNull()?.let {
                            // --- LÓGICA DEL INTENT CORREGIDA ---

                            val emailBody = "¡Hola Manos Locales! Necesito ayuda con lo siguiente:"

                            // Su único propósito es actuar como FILTRO para que solo se muestren apps de correo.
                            val selectorIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:".toUri()
                            }

                            // Se crea el Intent principal con la acción SEND, que sí respeta el asunto y el cuerpo.
                            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte.localhands@gmail.com")) // Usar la variable correcta
                                putExtra(Intent.EXTRA_SUBJECT, "Soporte - App Manos Locales")
                                putExtra(Intent.EXTRA_TEXT, emailBody)

                                // Usa el selector para garantizar compatibilidad con clientes de correo.
                                selector = selectorIntent
                            }

                            // Muestra un diálogo para que el usuario elija su cliente de correo.
                            // En nuestro caso, no se muestran alternativas ya que se eligió por defecto Gmail
                            context.startActivity(Intent.createChooser(emailIntent, "Enviar correo con:"))

                        }
                }
            }
        }
    )
}

@Composable
fun AlertsSwitchFavorites() {
    var checked by remember { mutableStateOf(true) }

    androidx.compose.material3.Switch(
        checked = checked, //Estado inicial del interruptor
        onCheckedChange = { //Es una devolución de llamada a la que se llama cuando cambia el estado del interruptor
            checked = it
        },
        thumbContent = if (checked) { //Para personalizar la apariencia del pulgar cuando está marcado.
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
