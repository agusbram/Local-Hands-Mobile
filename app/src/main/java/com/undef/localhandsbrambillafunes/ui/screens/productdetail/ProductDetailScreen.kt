package com.undef.localhandsbrambillafunes.ui.screens.productdetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.net.toUri
import com.undef.localhandsbrambillafunes.ui.viewmodel.profile.ProfileViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.util.PermissionManager
import kotlinx.coroutines.flow.firstOrNull
import java.io.File


/**
 * Pantalla de detalles del producto que muestra información completa del producto
 * seleccionado, incluyendo imágenes, descripción y opciones de contacto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    product: Product,
    favoriteViewModel: FavoriteViewModel = hiltViewModel<FavoriteViewModel>(),
    sellViewModel: SellViewModel = hiltViewModel<SellViewModel>(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    // Se obtiene el contexto para el Intent
    val context = LocalContext.current

    // Estado para guardar el email del vendedor encontrado
    var sellerEmail by remember { mutableStateOf("Cargando...") }

    // Obtenemos el email del usuario logueado (quien envía el correo)
    val editState by profileViewModel.editState.collectAsState()
    val currentUserEmail = editState.email

    // Estado para el guardar el teléfono del vendedor
    var sellerPhone by remember { mutableStateOf("") }

    // Buscamos el email y el teléfono del vendedor cuando arranca la pantalla y encuentra el ID del vendedor
    LaunchedEffect(product.ownerId) {
        val seller = sellViewModel.getSellerEmailById(product.ownerId!!).firstOrNull()
        sellerEmail = seller?.email ?: "Correo no encontrado"
        sellerPhone = seller?.phone ?: ""
    }


    // Estado para manejar la lista de imágenes del producto
    val productImages = remember { product.images }

    // Control del visor de imágenes
    val pagerState = rememberPagerState(pageCount = { productImages.size })

    // Cargamos la lista de productos favoritos actuales en la UI
    LaunchedEffect(Unit) {
        favoriteViewModel.loadFavorites()
    }
    val favorites by favoriteViewModel.favorites.collectAsState()

    // Estado para el favorito del producto de la base de datos
    val isFavorite = favorites.any { it.id == product.id }

    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // Permisos concedidos, proceder a compartir
            shareProductWithImageCompat(context, product, product.images, currentUserEmail)
        } else {
            Toast.makeText(
                context,
                "Se necesitan permisos para compartir imágenes",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
                        modifier = Modifier.clickable {
                            /**
                             * Se crea el Intent para abrir Gmail y enviar un correo prearmado
                             * creado con Kotlin de la siguiente manera:
                            * */
                            if (sellerEmail != "Cargando..." && sellerEmail != "Correo no encontrado") {

                                // Construimos el cuerpo del mensaje incluyendo al remitente
                                val emailBody = """
                                    Hola "${product.producer}",
                                    
                                    Te contacto desde "LocalHands" por tu producto "${product.name}".
                                    
                                    [Escribe aquí tu consulta]
                                    
                                    ---
                                    Datos del interesado:
                                    Enviado por: $currentUserEmail
                                """.trimIndent()

                                /**
                                * No es posible forzar a que la app te deje elegir automáticamente el remitente del correo
                                 * Solo es posible cambiarlo manualmente, por motivos de seguridad.
                                 * Para realizar esta mejora, se debe utilizar backend real, y esta app cuenta
                                 * con backend simulado con JSON DB.
                                * */
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    // "mailto:" asegura que solo se muestren apps de correo
                                    data = "mailto:".toUri()
                                    // Completa el destinatario del correo (el vendedor del producto seleccionado)
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(sellerEmail))
                                    // Crea el subtitulo del correo
                                    putExtra(Intent.EXTRA_SUBJECT, "Consulta LocalHands: ${product.name}")
                                    putExtra(Intent.EXTRA_TEXT, emailBody)
                                }

                                try {
                                    // Abrir selector de cuentas/apps de correo
                                    context.startActivity(Intent.createChooser(intent, "Enviar consulta con..."))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No tienes una app de correo configurada", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "No se pudo obtener el correo del vendedor", Toast.LENGTH_SHORT).show()
                            }
                        }
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
                        modifier = Modifier.clickable {
                            /**
                             * Se crea el Intent para abrir el Dial por defecto de Android
                             * para realizar llamada con el número del vendedor cargado previamente,
                             * agregar el contacto, o enviar un mensaje de texto.
                             * */
                            if (sellerPhone.isNotEmpty()) {
                                /**
                                 * Se normaliza el número de télefono, sabiendo que el mismo corresponde
                                 * a Argentina
                                 * */
                                val normalizedPhone = normalizePhoneAR(sellerPhone)

                                /**
                                 * ACTION_DIAL abre el marcador con el número puesto
                                 * pero no inicia la llamada automáticamente (es más seguro)
                                 * */
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = "tel:$normalizedPhone".toUri()
                                }

                                // Abrir la pantalla para realizar llamada, agregar contacto o enviar mensaje de texto
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "El vendedor no tiene teléfono registrado", Toast.LENGTH_SHORT).show()
                            }
                        }
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
                        modifier = Modifier.clickable {
                            /**
                             * Se crea un mensaje de texto formateado con la información esencial
                             * del producto para compartirlo a través de apps externas (WhatsApp, Instagram, etc.)
                             */
                            // Verificar permisos antes de compartir
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (!PermissionManager.hasImagePermission(context)) {
                                    // Solicitar permisos antes de compartir
                                    permissionLauncher.launch(PermissionManager.getRequiredPermissions())
                                } else {
                                    // Ya tiene permisos, compartir directamente
                                    shareProductWithImageCompat(context, product, product.images, currentUserEmail)
                                }
                            } else {
                                // Para versiones anteriores a Android 13, compartir directamente
                                shareProductWithImageCompat(context, product, product.images, currentUserEmail)
                            }
                        }
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


/**
 * Comparte la información de un producto mediante un Intent, incluyendo opcionalmente
 * una imagen, asegurando compatibilidad con distintas versiones de Android.
 *
 * Si existe al menos una imagen válida, se adjunta al contenido compartido usando
 * un `FileProvider`. En caso contrario, se comparte únicamente el texto descriptivo
 * del producto.
 *
 * @param context Contexto desde el cual se lanza el Intent de compartición.
 * @param product Objeto [Product] que contiene la información principal del producto.
 * @param productImages Lista de rutas de imágenes asociadas al producto.
 *                      Se utilizará únicamente la primera imagen válida.
 * @param currentUserEmail Correo electrónico del usuario actual (no utilizado
 *                         directamente en la implementación actual, pero disponible
 *                         para futuras extensiones).
 */
fun shareProductWithImageCompat(
    context: Context,
    product: Product,
    productImages: List<String>,
    currentUserEmail: String
) {
    // Se crea el texto descriptivo a compartir del producto
    val shareText = """
        ¡Mira lo que encontré en LocalHands! 
        
        🎁 Producto: *${product.name}*
        💰 Precio: $${product.price}
        📍 Ubicación: ${product.location}
        👤 Vendedor: ${product.producer}
        
        Descripción: ${product.description}
        
        _Enviado desde "LocalHands"_
    """.trimIndent()

    // Se crea el Intent para compartir el producto a través del botón de Share
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        // Intentar adjuntar imagen
        val imageUri = getImageUriForSharing(context, productImages)

        if (imageUri != null) {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
    }

    val shareIntent = Intent.createChooser(sendIntent, "Compartir producto")

    try {
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo compartir", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Obtiene de forma segura un [Uri] compartible para la primera imagen válida
 * de la lista proporcionada.
 *
 * La función:
 * - Limpia el prefijo `file://` si existe en la ruta.
 * - Verifica que el archivo exista físicamente.
 * - Genera un `content://Uri` usando un [FileProvider] configurado en la aplicación.
 *
 * En caso de error o si no se encuentra una imagen válida, retorna `null`.
 *
 * @param context Contexto necesario para acceder al [FileProvider].
 * @param productImages Lista de rutas de imágenes del producto.
 * @return Un [Uri] seguro para compartir la imagen o `null` si no es posible obtenerlo.
 */
private fun getImageUriForSharing(context: Context, productImages: List<String>): Uri? {
    return try {
        productImages.firstOrNull()?.let { imagePath ->
            val cleanedPath = if (imagePath.startsWith("file://")) {
                imagePath.replace("file://", "")
            } else {
                imagePath
            }

            // Obtiene el archivo y, si existe, lo convierte a un Uri para utilizar el mismo en el compartido del Intent
            val file = File(cleanedPath)
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Normaliza un número telefónico argentino para uso local.
 *
 * Convierte formatos comunes (+54, 9, 0, espacios, guiones, paréntesis)
 * a un número válido para el marcador telefónico nacional.
 *
 * Ejemplos:
 * +54 9 11 3456-7890 → 1134567890
 * 011 3456 7890     → 1134567890
 *
 * @param phone Número telefónico en formato libre.
 * @return Número normalizado (solo dígitos) o cadena vacía si es inválido.
 */
fun normalizePhoneAR(phone: String): String {
    // Eliminar todo lo que no sea dígito
    var clean = phone.replace(Regex("[^0-9]"), "")

    // Eliminar código país 54 si existe
    if (clean.startsWith("54")) {
        clean = clean.removePrefix("54")
    }

    // Eliminar 9 (usado para móviles internacionales)
    if (clean.startsWith("9")) {
        clean = clean.removePrefix("9")
    }

    // Eliminar 0 inicial del código de área
    if (clean.startsWith("0")) {
        clean = clean.removePrefix("0")
    }

    // Validación mínima (10 dígitos típico en AR)
    return if (clean.length in 10..11) clean else ""
}