package com.undef.localhandsbrambillafunes.ui.screens.entrepreneur

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.ui.viewmodel.products.ProductViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.session.SessionViewModel
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
    sessionViewModel: SessionViewModel = hiltViewModel<SessionViewModel>()
) {
    /*Para obtener el id actual del usuario reflejado en la UI en tiempo real*/
    val currentUserIdState = remember { mutableStateOf<Int?>(null) }

    /*Llamamos a una funcion suspend con corrutinas para obtener el currentUserId*/
    LaunchedEffect(Unit) {
        val currentUserId = sessionViewModel.getCurrentUserId()
        currentUserIdState.value = currentUserId
    }

    //Todos los productos actuales
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
    var producer by remember { mutableStateOf(originalProduct?.producer ?: "") }
    var category by remember { mutableStateOf(originalProduct?.category ?: "") }

    // Estado de imágenes seleccionadas en la pantalla de edición/creación
    var images by remember { mutableStateOf(originalProduct?.images ?: emptyList()) }
    var price by remember { mutableStateOf(originalProduct?.price?.toString() ?: "") }
    var location by remember { mutableStateOf(originalProduct?.location ?: "") }

    val isEditing = originalProduct != null
    // Validaciones
    val isNameValid = isValidTextField(name)
    val isDescriptionValid = isValidTextField(description)
    val isProducerValid = isValidTextField(producer)
    val isPriceValid = isValidPrice(price)
    // Formulario válido solo si todo está correcto
    val isFormValid = isNameValid && isDescriptionValid && isProducerValid && isPriceValid &&
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
            OutlinedTextField(
                value = producer,
                onValueChange = { producer = it },
                label = { Text("Productor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedLabelColor = if (isProducerValid) Color.Green else Color.Red,
                    focusedIndicatorColor = if (isProducerValid) Color.Green else Color.Red,
                    unfocusedIndicatorColor = if (isProducerValid) Color.Green.copy(0.6f) else Color.Red.copy(0.6f)
                )
            )
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
            LocationDropdown(selectedLocation = location, onLocationSelected = { location = it })

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
                            ownerId = currentUserIdState.value ?: originalProduct?.ownerId
                        )
                        if (isEditing) {
                            productViewModel.updateProduct(entity)
                        } else {
                            productViewModel.addProduct(entity)
                            productViewModel.addProductByApi(entity)
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
                                productViewModel.deleteProduct(it)
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
 * Composable que muestra un menú desplegable de selección de localidades de Córdoba, Argentina.
 *
 * Esta función permite al usuario seleccionar su ubicación entre una lista completa de localidades
 * de la provincia de Córdoba. La búsqueda es dinámica: al escribir en el campo, la lista se filtra automáticamente.
 *
 * ## Características:
 * - Lista completa de localidades de Córdoba.
 * - Filtro en tiempo real para facilitar la búsqueda.
 * - Integración con formularios de productos u otros usos.
 *
 * @param selectedLocation Valor actual seleccionado por el usuario.
 * @param onLocationSelected Función callback que se ejecuta al seleccionar una nueva localidad.
 */
@Composable
fun LocationDropdown(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    val allLocations = listOf(
        "Córdoba Capital", "Villa Carlos Paz", "Alta Gracia", "Jesús María", "Río Cuarto",
        "Villa María", "Villa Dolores", "Villa General Belgrano", "Cosquín", "La Cumbre",
        "Capilla del Monte", "Mina Clavero", "San Marcos Sierras", "Villa Allende", "Unquillo",
        "Salsipuedes", "Colonia Caroya", "La Falda", "Malagueño", "Monte Cristo", "Río Ceballos",
        "Dean Funes", "Bell Ville", "Arroyito", "San Francisco", "Leones", "Corral de Bustos",
        "Laboulaye", "Huinca Renancó", "La Carlota", "Cruz del Eje", "Marcos Juárez", "General Deheza",
        "General Cabrera", "Morteros", "Oncativo", "Las Varillas", "Villa Nueva", "Pilar", "Villa del Rosario",
        "Laguna Larga", "Tancacha", "Oliva", "La Calera", "Monte Maíz", "Embalse", "La Paz", "Almafuerte",
        "Bialet Massé", "Santa Rosa de Calamuchita", "Villa Rumipal", "Villa Yacanto", "Nono", "Tanti"
    )

    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filteredLocations = remember(searchText) {
        allLocations.filter { it.contains(searchText, ignoreCase = true) }
    }

    Box {
        OutlinedTextField(
            value = searchText.ifBlank { selectedLocation },
            onValueChange = {
                searchText = it
                expanded = true
            },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = false,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Expandir"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredLocations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location) },
                    onClick = {
                        onLocationSelected(location)
                        searchText = location
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 *
 * MultiImagePickerField: componente reutilizable para selección de imágenes.
 * Al seleccionar una o varias imágenes, se guardan en almacenamiento interno
 * y se actualiza el estado `images`, listo para ser persistido en Room.
 * Selector visual para múltiples imágenes desde la galería del dispositivo.
 *
 * Esta función composable permite al usuario seleccionar múltiples imágenes de su galería utilizando
 * `ActivityResultContracts.GetMultipleContents`. Las imágenes seleccionadas se visualizan en una fila horizontal.
 *
 * Está diseñada para integrarse con formularios o pantallas de edición de contenido (por ejemplo, productos
 * con imágenes en una aplicación de marketplace).
 *
 * ## Características:
 * - Permite seleccionar múltiples imágenes a la vez.
 * - Muestra las imágenes seleccionadas en miniaturas (LazyRow).
 * - Utiliza `rememberLauncherForActivityResult` para gestionar el resultado de la selección.
 *
 * ## Parámetros:
 * @param selectedUris Lista actual de imágenes seleccionadas (como URIs).
 * @param onImagesSelected Función callback que se invoca con la nueva lista de URIs seleccionadas cuando el usuario elige imágenes.
 *
 * ## Ejemplo de uso:
 * ```
 * var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
 * MultiImagePickerField(
 *     selectedUris = imageUris,
 *     onImagesSelected = { imageUris = it }
 * )
 * ```
 *
 * ## Consideraciones:
 * - El parámetro `imageUris.map { it.toString() }` puede usarse para almacenar las rutas en Room.
 * - En emuladores o dispositivos físicos se requiere acceso al sistema de archivos (la galería).
 */
@Composable
fun MultiImagePickerField(
    selectedPaths: List<String>,
    onImagesSelected: (List<String>) -> Unit
) {
    val context = LocalContext.current

    // Launcher para seleccionar múltiples imágenes desde el explorador
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val paths = uris.mapNotNull { uri ->
            copyUriToInternalStorage(context, uri)
        }
        if (paths.isNotEmpty()) {
            onImagesSelected(paths)
        }
    }

    Column {
        // Botón que abre el selector de imágenes
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar imágenes")
        }

        // Vista previa horizontal de las imágenes seleccionadas
        if (selectedPaths.isNotEmpty()) {
            LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                items(selectedPaths) { path ->
                    AsyncImage(
                        model = File(path), // Carga desde archivo local
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
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




