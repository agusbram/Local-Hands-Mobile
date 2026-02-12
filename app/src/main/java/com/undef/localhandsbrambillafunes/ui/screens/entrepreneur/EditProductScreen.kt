package com.undef.localhandsbrambillafunes.ui.screens.entrepreneur

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.repository.SellerRepository
import com.undef.localhandsbrambillafunes.data.repository.UserPreferencesRepository
import com.undef.localhandsbrambillafunes.ui.components.LocationMapSelector
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.products.ProductViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.sell.SellViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.session.SessionViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Pantalla de edición o creación de productos en la aplicación.
 *
 * Esta función composable permite modificar un producto existente o crear uno nuevo, dependiendo
 * de si se encuentra un producto con el `productId` proporcionado.
 *
 * Se utiliza el patrón MVVM, obteniendo los productos desde el [ProductViewModel] y manipulándolos
 * mediante las funciones `addProduct`, `updateProduct` y `deleteProduct`.
 *
 * ## Características:
 * - Carga automáticamente los datos del producto si el `productId` existe.
 * - Presenta campos de entrada editables para cada atributo del producto.
 * - Permite crear, actualizar o eliminar un producto desde la UI.
 * - Navega de regreso a la pantalla anterior tras finalizar cualquier acción.
 *
 * @param navController Controlador de navegación utilizado para volver a la pantalla anterior.
 * @param productId ID del producto a editar. Si no existe, se asume que se está creando un nuevo producto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    productId: Int,
    productViewModel: ProductViewModel = hiltViewModel<ProductViewModel>(),
    sellViewModel: SellViewModel = hiltViewModel<SellViewModel>(),
    sessionViewModel: SessionViewModel = hiltViewModel<SessionViewModel>()
) {
    // Para obtener el id actual del usuario reflejado en la UI en tiempo real
    val currentUserIdState = remember { mutableStateOf<Int?>(null) }

    // Estado para almacenar el nombre del emprendimiento del vendedor
    val entrepreneurshipState = remember { mutableStateOf("") }

    // Se observan cambios en tiempo real del entrepreneurship
    val entrepreneurshipFromViewModel by sellViewModel.entrepreneurshipName.collectAsState()

    // Llamamos a una funcion suspend con corrutinas para obtener el currentUserId

    /**
     * Obtiene el contexto actual de Compose, necesario para lanzar el Intent
     * de envío de correo electrónico.
     */
    val context = LocalContext.current

    /**
     * Efecto ejecutado una única vez cuando el Composable entra en composición.
     *
     * Este bloque cumple tres responsabilidades principales:
     *
     * 1. Inicialización de datos del usuario:
     *    - Obtiene el identificador del usuario actual desde SessionViewModel.
     *    - Actualiza el estado local `currentUserIdState`.
     *    - Registra el ID en el log para fines de depuración.
     *
     * 2. Carga de información del emprendimiento:
     *    - Solicita al SellViewModel el nombre del emprendimiento.
     *    - Actualiza el estado local `entrepreneurshipState` para su uso en la UI.
     *
     * 3. Observación de eventos de notificación por correo:
     *    - Escucha el StateFlow `emailNotificationEvent` del ProductViewModel.
     *    - Cuando se recibe un evento válido:
     *        a) Ejecuta la función que lanza el Intent de envío de correo.
     *        b) Resetea el evento en el ViewModel para evitar reenvíos
     *           accidentales durante recomposiciones.
     *
     * Este enfoque mantiene la lógica de negocio en los ViewModels,
     * mientras que las acciones dependientes del contexto (como abrir
     * un Intent) se ejecutan en la capa de UI.
     */
    LaunchedEffect(Unit) {
        Log.d("EditProductScreen", "--- INICIANDO CARGA ---")
        try {
            val userId = sessionViewModel.getCurrentUserId()
            currentUserIdState.value = userId
            Log.d("EditProductScreen", "Usuario autenticado con ID: $userId")

            val loaded = sellViewModel.loadEntrepreneurshipForUI()
            Log.d("EditProductScreen", "Emprendimiento cargado: '$loaded'")

            entrepreneurshipState.value = loaded
        } catch (e: Exception) {
            Log.e("EditProductScreen", "ERROR CRITICO: ${e.message}")
            // Si hay error, quizás quieras mandar al usuario al Login
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.emailNotificationEvent.collect { event ->
            event?.let { (emails, entrepreneurship, product) ->
                // Llamamos a la función de envío
                sendEmailToInterestedUsers(context, emails, entrepreneurship, product)
                // Reseteamos el evento en el ViewModel
                productViewModel.resetEmailEvent()
            }
        }
    }

    // Actualizar entrepreneurshipState cuando cambie entrepreneurship
    LaunchedEffect(entrepreneurshipFromViewModel) {
        if (entrepreneurshipFromViewModel.isNotEmpty() && entrepreneurshipFromViewModel != entrepreneurshipState.value) {
            entrepreneurshipState.value = entrepreneurshipFromViewModel
        }
    }

    // Usar entrepreneurshipState.value como el producer
    val producer = entrepreneurshipState.value

    // Mostrar advertencia si no hay entrepreneurship
    if (producer.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No se encontró tu emprendimiento",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Por favor, actualiza tu perfil de vendedor primero",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate(AppScreens.ProfileScreen.route) }
            ) {
                Text("Ir a mi perfil")
            }
        }
        return
    }

    // Todos los productos actuales
    val allProducts by productViewModel.products.collectAsState()

    // Estado para evitar recomposición hasta que se cargue el producto
    var productLoaded by remember { mutableStateOf(false) }
    var originalProduct: Product? by remember { mutableStateOf(null) }

    LaunchedEffect(allProducts) {
        val foundProduct = allProducts.find { it.id == productId }
        if (foundProduct != null) {
            originalProduct = foundProduct
            productLoaded = true
        }
    }

    if (!productLoaded && productId != 0) {
        // Mostrar carga o espera si es edición
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Variables de estado que contienen los campos editables
    // Si es un producto nuevo, los campos están vacíos
    var name by remember { mutableStateOf(originalProduct?.name ?: "") }
    var description by remember { mutableStateOf(originalProduct?.description ?: "") }

    // Estado de imágenes seleccionadas en la pantalla de edición/creación
    var category by remember { mutableStateOf(originalProduct?.category ?: "") }
    var images by remember { mutableStateOf(originalProduct?.images ?: emptyList()) }
    var price by remember { mutableStateOf(originalProduct?.price?.toString() ?: "") }
    var location by remember { mutableStateOf(originalProduct?.location ?: "") }
    
    /**
     * Coordenadas geográficas del producto para filtrado por proximidad.
     * Se capturan cuando el usuario selecciona una ubicación mediante Google Maps.
     */
    var productLatitude by remember { mutableStateOf(originalProduct?.latitude ?: 0.0) }
    var productLongitude by remember { mutableStateOf(originalProduct?.longitude ?: 0.0) }

    val isEditing = originalProduct != null
    // Validaciones
    val isNameValid = isValidTextField(name)
    val isDescriptionValid = isValidTextField(description)
    val isPriceValid = isValidPrice(price)
    // Formulario válido solo si todo está correcto
    val isFormValid = isNameValid && isDescriptionValid && isPriceValid &&
            category.isNotEmpty() && location.isNotEmpty() && images.isNotEmpty()


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
                        Text(
                            if (isEditing) "Editar producto" else "Nuevo producto",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF242424),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // Estructura de la pantalla con inputs y acciones
        Column(
            Modifier
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            // Campos de entrada para cada atributo del producto
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = if (isNameValid) Color.Green else Color.Red,
                    focusedIndicatorColor = if (isNameValid) Color.Green else Color.Red,
                    unfocusedIndicatorColor = if (isNameValid) Color.Green.copy(0.6f) else Color.Red.copy(0.6f)
                )
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = if (isDescriptionValid) Color.Green else Color.Red,
                    focusedIndicatorColor = if (isDescriptionValid) Color.Green else Color.Red,
                    unfocusedIndicatorColor = if (isDescriptionValid) Color.Green.copy(0.6f) else Color.Red.copy(0.6f)
                )
            )

            /**
             * Campo de emprendimiento/productor actual.
             * No es modificable. Se sincroniza con el emprendimiento del vendedor actual.
             * */
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Productor (Emprendimiento)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = producer,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            CategoryDropdown(
                selectedCategory = category,
                onCategorySelected = { category = it }
            )

            // Botón para agregar varias imágenes del producto a vender
            MultiImagePickerField(
                selectedPaths = images,
                onImagesSelected = { images = it }
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = if (isPriceValid) Color.Green else Color.Red,
                    focusedIndicatorColor = if (isPriceValid) Color.Green else Color.Red,
                    unfocusedIndicatorColor = if (isPriceValid) Color.Green.copy(0.6f) else Color.Red.copy(0.6f)
                )
            )
            LocationDropdown(
                selectedLocation = location, 
                preselectedFromSeller = producer, // Pre-seleccionar con ubicación del emprendimiento
                context = context,
                onLocationSelected = { newLocation, latitude, longitude ->
                    location = newLocation
                    productLatitude = latitude
                    productLongitude = longitude
                }
            )

            Spacer(Modifier.height(16.dp))

            Row {
                // Botón para guardar o crear producto
                Button(
                    onClick = {
                        val entity = Product(
                            id = originalProduct?.id ?: 0,
                            name = name,
                            description = description,
                            producer = producer,
                            category = category,
                            images = images,
                            price = price.toDoubleOrNull() ?: 0.0,
                            location = location,
                            latitude = productLatitude,
                            longitude = productLongitude,
                            ownerId = currentUserIdState.value ?: originalProduct?.ownerId
                        )
                        if (isEditing) {
                            productViewModel.updateProductSyncApi(entity)
                        } else {
                            productViewModel.addProductSyncApi(entity)
                        }
                        navController.popBackStack() // Vuelve a la lista
                    },
                    enabled = isFormValid
                ) {
                    Text(if (isEditing) "Guardar cambios" else "Crear producto")
                }

                Spacer(Modifier.width(16.dp))

                // Botón para eliminar producto (solo en modo edición)
                if (isEditing) {
                    Button(
                        onClick = {
                            originalProduct?.let {
                                productViewModel.deleteProductSyncApi(it)
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

/**
 * Composable que renderiza un menú desplegable para seleccionar una categoría de producto.
 *
 * Este componente muestra un `OutlinedTextField` de solo lectura que, al ser presionado, despliega
 * una lista de categorías predefinidas para que el usuario seleccione una. La categoría seleccionada
 * se muestra como texto y se propaga mediante el callback `onCategorySelected`.
 *
 * ## Uso:
 * Ideal para formularios de edición o creación de productos donde se necesita restringir la entrada
 * a un conjunto conocido de categorías válidas.
 *
 * @param selectedCategory Categoría actualmente seleccionada. Se muestra como valor en el campo de texto.
 * @param onCategorySelected Función lambda que se invoca cuando el usuario selecciona una nueva categoría.
 *
 * ## Categorías disponibles:
 * - "Alimentos"
 * - "Textiles"
 * - "Artesanías"
 * - "Cosmética"
 *
 * @sample CategoryDropdown("Alimentos") { newCategory -> /* handle update */ }
 */
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Alimentos", "Textiles", "Artesanías", "Cosmética")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {}, 
            readOnly = true,
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Composable que muestra un menú desplegable de selección de localidades
 * con opción de seleccionar desde Google Maps.
 *
 * @param selectedLocation Valor actual seleccionado
 * @param preselectedFromSeller Nilai pre-seleccionada del emprendimiento (para autocompletar)
 * @param context Contexto para Google Maps
 * @param onLocationSelected Callback con la ubicación seleccionada
 */
@Composable
fun LocationDropdown(
    selectedLocation: String,
    preselectedFromSeller: String = "",
    context: android.content.Context,
    onLocationSelected: (String, Double, Double) -> Unit
) {
    val cordobaLocations = listOf(
        "Córdoba", "Villa Carlos Paz", "La Falda", "Jesús María", "Alta Gracia", "Río Cuarto",
        "Villa María", "San Francisco", "Bell Ville", "Marcos Juárez", "Cruz del Eje", "Mina Clavero"
    )

    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(selectedLocation) }
    var showMapSelector by remember { mutableStateOf(false) }

    // Auto-completar con ubicación del emprendimiento si está disponible  
    LaunchedEffect(preselectedFromSeller) {
        if (preselectedFromSeller.isNotEmpty() && selectedLocation.isEmpty()) {
            searchText = preselectedFromSeller
            onLocationSelected(preselectedFromSeller, 0.0, 0.0)
        }
    }

    val filteredLocations = if (searchText.isEmpty() || searchText == selectedLocation) {
        cordobaLocations
    } else {
        cordobaLocations.filter { it.contains(searchText, ignoreCase = true) }
    }

    Box {
        OutlinedTextField(
            value = searchText,
            onValueChange = { 
                searchText = it
                expanded = true
            },
            label = { Text("Localidad") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded && filteredLocations.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            // Opción de seleccionar desde Google Maps
            DropdownMenuItem(
                text = { Text("📍 Seleccionar en Google Maps") },
                onClick = {
                    showMapSelector = true
                    expanded = false
                }
            )
            
            // Separador
            HorizontalDivider()
            
            filteredLocations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location) },
                    onClick = {
                        searchText = location
                        onLocationSelected(location, 0.0, 0.0)  // Coordenadas por defecto para ubicaciones preseleccionadas
                        expanded = false
                    }
                )
            }
        }
    }

    // Selector de ubicación con Google Maps
    if (showMapSelector) {
        LocationMapSelector(
            title = "Selecciona la localidad del producto",
            initialAddress = searchText,
            context = context,
            onLocationSelected = { selectedAddress, latitude, longitude ->
                searchText = selectedAddress
                onLocationSelected(selectedAddress, latitude, longitude)
                showMapSelector = false
            },
            onDismiss = { showMapSelector = false },
            confirmButtonText = "Confirmar Localidad"
        )
    }
}

/**
 * Composable que permite al usuario seleccionar múltiples imágenes desde la galería.
 *
 * Muestra una lista horizontal de las imágenes seleccionadas y un botón para añadir más.
 *
 * @param selectedPaths Lista de URIs (como Strings) de las imágenes ya seleccionadas.
 * @param onImagesSelected Callback que se invoca con la nueva lista de URIs cuando el usuario selecciona imágenes.
 */
@Composable
fun MultiImagePickerField(selectedPaths: List<String>, onImagesSelected: (List<String>) -> Unit) {
    val context = LocalContext.current

    // Launcher para seleccionar múltiples imágenes
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            // Convertir URIs a Strings y añadir a la lista existente
            val newPaths = uris.map { uri ->
                // Copiar el archivo a almacenamiento interno para obtener una ruta persistente
                File(context.cacheDir, "temp_${System.currentTimeMillis()}").apply {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }.absolutePath
            }
            onImagesSelected(selectedPaths + newPaths)
        }
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        // Botón para lanzar el selector de imágenes
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar Imágenes")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LazyRow para mostrar las previsualizaciones de las imágenes seleccionadas
        LazyRow {
            items(selectedPaths) {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * Valida si el campo de texto contiene por lo menos 10 caracteres
 * @input Texto a validar
 * */
fun isValidTextField(input: String): Boolean = input.trim().length >= 10

/**
 * Valida si el precio es un número positivo
 * @price Precio a validar
 * */
fun isValidPrice(price: String): Boolean =
    price.toDoubleOrNull()?.let { it > 0 } == true


/**
 * Lanza un Intent para enviar un correo electrónico a los usuarios interesados
 * en los productos de un determinado emprendimiento.
 *
 * Funcionamiento:
 * - Si la lista de correos está vacía, muestra un mensaje informativo y finaliza.
 * - Convierte la lista de destinatarios a un arreglo de Strings.
 * - Crea un Intent con acción ACTION_SENDTO y esquema "mailto:" para asegurar
 *   que únicamente se abran aplicaciones de correo electrónico.
 * - Configura los destinatarios, asunto y cuerpo del mensaje.
 * - Abre un selector de aplicaciones disponibles para el envío.
 *
 * En caso de que no exista ninguna aplicación de correo instalada,
 * se captura la excepción y se muestra un mensaje al usuario.
 *
 * @param context Contexto necesario para lanzar el Intent y mostrar Toast.
 * @param emails Lista de direcciones de correo de los destinatarios.
 * @param entrepreneurshipName Nombre del emprendimiento utilizado en el asunto del correo.
 */
fun sendEmailToInterestedUsers(context: Context, emails: List<String>, entrepreneurshipName: String, product: Product) {
    if (emails.isEmpty()) {
        Toast.makeText(context, "No hay usuarios con tus productos en favoritos", Toast.LENGTH_SHORT).show()
        return
    }

    // Convertimos la lista de emails a un array de Strings
    val recipients = emails.toTypedArray()

    // Armamos el cuerpo del mensaje con los datos dinámicos
    val emailBody = """
        Hola! Tenemos nuevas noticias para ti!
        Hemos publicado un nuevo producto con los siguientes datos:
        
        Nombre: ${product.name}
        Descripción: ${product.description}
        Precio: $${product.price}
        
        Para saber más, visita nuestro emprendimiento: $entrepreneurshipName.""".trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        // "mailto:" asegura que solo se abran apps de correo electrónico
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, recipients)
        putExtra(Intent.EXTRA_SUBJECT, "Novedades de $entrepreneurshipName")
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Enviar correo con..."))
    } catch (e: Exception) {
        Toast.makeText(context, "No tienes aplicaciones de correo instaladas", Toast.LENGTH_SHORT).show()
    }
}