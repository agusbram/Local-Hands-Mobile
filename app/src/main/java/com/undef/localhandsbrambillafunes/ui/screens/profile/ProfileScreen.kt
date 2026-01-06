package com.undef.localhandsbrambillafunes.ui.screens.profile

import android.net.Uri

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.profile.ProfileViewModel
import com.undef.localhandsbrambillafunes.ui.viewmodel.profile.UiEvent
import com.undef.localhandsbrambillafunes.ui.viewmodel.settings.SettingsViewModel
import com.undef.localhandsbrambillafunes.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController,
                  settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>(),
                  profileViewModel: ProfileViewModel = hiltViewModel<ProfileViewModel>()
) {
    // Se observa el estado de los campos de edición en tiempo real
    val editState by profileViewModel.editState.collectAsState()

    // Se observa el role del usuario en tiempo real
    val userRole by profileViewModel.userRole.collectAsState()

    // Para crear los Toast
    val context = LocalContext.current

    // Leemos el valor de la ubicacion en tiempo real
    val userCity by settingsViewModel.userLocation.collectAsState()

    //Para validar los datos del usuario
    val isNameValid = editState.name.length >= 3
    val isLastNameValid = editState.lastName.length >= 3
    val isEmailValid = isValidEmail(editState.email)
    val isPhoneValid = isValidPhone(editState.phone)
    val isAddressValid = editState.address.length >= 5
    val isEntrepreneurshipValid = editState.entrepreneurship.length >= 4

    //Para validar que el formulario esté completo para guardar los cambios
    val isFormValid = isNameValid && isLastNameValid && isEmailValid && isPhoneValid && isAddressValid


    //Variables para dialog de contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    //Variable para dialog de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Variable para dialog de eliminación de cuenta de usuario/vendedor
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Variable para dialog de eliminación de foto
    var showDeletePhotoDialog by remember { mutableStateOf(false) }

    val profileState by profileViewModel.uiState.collectAsState()

    // Launcher que abre la galería
    val imagePickerLauncher = rememberLauncherForActivityResult( //Abre el explorador de archivos para seleccionar una imagen
        contract = ActivityResultContracts.GetContent() //Recibe la ruta que se define en launch mas abajo con el launcher
    ) { uri: Uri? ->
        // Comprueba que el usuario realmente seleccionó una imagen
        if(uri != null) {
            profileViewModel.changeProfilePicture(uri)
        }
    }

    // Refrescar la foto al entrar a la pantalla por única vez
    LaunchedEffect(Unit) {
        profileViewModel.refreshPhotoUrl()
    }

    /**
     * --- ESCUCHA DE EVENTOS DE LA UI ---
     * LaunchedEffect se suscribe al flujo de eventos del ViewModel (ProfileViewModel para en este caso).
     * 'key1 = true' significa que se ejecutará una sola vez y se mantendrá escuchando.
     */
    LaunchedEffect(key1 = true) {
        profileViewModel.uiEventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()

                    // Si el mensaje es sobre la foto, refrescar
                    if (event.message.contains("foto", ignoreCase = true) ||
                        event.message.contains("photo", ignoreCase = true)) {
                        profileViewModel.refreshPhotoUrl()
                    }
                }
                is UiEvent.NavigateAndClearStack -> {
                    // Navega y limpia todo el backstack
                    navController.navigate(event.route) {
                        // Limpia TODO el backstack hasta la ruta raíz
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Obtener el estado del perfil en tiempo real
                    val profileState by profileViewModel.uiState.collectAsState()

                    // Obtener la URL de manera segura
                    val currentPhotoUrl = profileState.photoUrl

                    // Muestra la foto de perfil con una animación de carga
                    // Permite cargar la foto desde el almacenamiento interno del dispositivo emulador
                    AsyncImage(
                        model = if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty()) {
                            val file = File(currentPhotoUrl)
                            // Verifica que la URL de la foto del perfil existe antes de cargarlo
                            if (file.exists()) {
                                file
                            } else {
                                R.drawable.ic_profile_placeholder
                            }
                        } else {
                            R.drawable.ic_profile_placeholder
                        },
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                if (currentPhotoUrl != null && currentPhotoUrl.isNotEmpty()) {
                                    showDeletePhotoDialog = true
                                } else {
                                    imagePickerLauncher.launch("image/*")
                                }
                            },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_profile_placeholder),
                        error = painterResource(id = R.drawable.ic_profile_placeholder)
                    )
                }

                // Texto explicativo debajo de la foto de perfil de usuario/vendedor
                Text(
                    text = profileState.photoUrl?.let { url ->
                        if (url.isNotEmpty()) {
                            val file = File(url)
                            // Muestra distinto texto dependiendo de si existe o no la URL de la foto de perfil
                            if (file.exists()) {
                                "Toca la foto para cambiarla o eliminarla"
                            } else {
                                "Toca para agregar una foto de perfil"
                            }
                        } else {
                            "Toca para agregar una foto de perfil"
                        }
                    } ?: "Toca para agregar una foto de perfil",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(12.dp))

                //Info personal
                //Campos editables: nombre completo, teléfono, domicilio y ciudad, etc

                // Si el email está vacío, significa que los datos aún no se han cargado.
                if (editState.email.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Cuando los datos están cargados, mostramos los campos.

                    // Campo editable del atributo nombre
                    EditableProfileItem(
                        label = "Nombre",
                        value = editState.name,
                        isValid = isNameValid,
                        onValueChange = { newValue ->
                            val newState = editState.copy(name = newValue)
                            profileViewModel.onFieldChange(newState)
                        }
                    )

                    // Campo editable del atributo apellido
                    EditableProfileItem(
                        label = "Apellido",
                        value = editState.lastName,
                        isValid = isLastNameValid,
                        onValueChange = { newValue ->
                            val newState = editState.copy(lastName = newValue)
                            profileViewModel.onFieldChange(newState)
                        },
                    )

                    // Campo editable del atributo email
                    EditableProfileItem(
                        label = "Correo Electrónico",
                        value = editState.email,
                        isValid = isEmailValid,
                        onValueChange = { newValue ->
                            val newState = editState.copy(email = newValue)
                            profileViewModel.onFieldChange(newState)
                        }
                    )

                    // Campo editable del atributo domicilio
                    EditableProfileItem(
                        label = "Domicilio",
                        value = editState.address,
                        isValid = isAddressValid,
                        onValueChange = { newValue ->
                            val newState = editState.copy(address = newValue)
                            profileViewModel.onFieldChange(newState)
                        }
                    )

                    // Campo editable del atributo teléfono
                    EditableProfileItem(
                        label = "Teléfono",
                        value = editState.phone,
                        isValid = isPhoneValid,
                        onValueChange = { newValue ->
                            val newState = editState.copy(phone = newValue)
                            profileViewModel.onFieldChange(newState)
                        }
                    )

                    /**
                    * Campo de ciudad actual.
                     * Solamente modificable desde la pantalla de configuración
                    * */
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Ciudad",
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
                                text = userCity,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Campo editable del atributo emprendimiento (se muestra únicamente si es vendedor)
                    if (userRole == UserRole.SELLER) {
                        Spacer(modifier = Modifier.height(8.dp))
                        EditableProfileItem(
                            label = "Emprendimiento",
                            value = editState.entrepreneurship,
                            isValid = isEntrepreneurshipValid,
                            onValueChange = { newValue ->
                                val newState = editState.copy(entrepreneurship = newValue)
                                profileViewModel.onFieldChange(newState)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Texto clickeable para cambiar la contraseña
                Text("Cambiar contraseña", color = Color.Blue, modifier = Modifier.clickable {
                    showPasswordDialog = true
                })

                // Texto clickeable para ver mis productos
                Text("Mis productos", color = Color.Blue, modifier = Modifier.clickable {
                    Toast.makeText(context, "Mis productos (futuro)", Toast.LENGTH_SHORT).show()
                })
                // Texto clickeable para eliminar la cuenta
                Text("Eliminar cuenta", color = Color.Red, modifier = Modifier.clickable {
                    showDeleteConfirmDialog = true
                })

                Spacer(Modifier.height(32.dp))

                // Botón para guardar cambios
                Button(
                    onClick = {
                        // Se guardan los datos del perfil del usuario en la tabla User de la BD de Room
                        profileViewModel.saveChanges()
                    },
                    enabled = isFormValid, //Para poder clickear el boton debe estar previamente validado el formulario
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Guardar cambios")
                }

                //Botón para cerrar sesión
                Button(
                    onClick = { showLogoutDialog = true }, //Te redirige a la pantalla de login
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }

                // Dialog cambiar contraseña
                if (showPasswordDialog) {
                    ChangePasswordDialog(
                        newPassword = newPassword,
                        repeatPassword = repeatPassword,
                        onPasswordChange = { newPassword = it },
                        onRepeatChange = { repeatPassword = it },
                        //Esta accion ocurre cuando el usuario presiona "cancelar" o cierra el diálogo: se oculta el diálogo y se limpian los campos de contraseña
                        onDismiss = {
                            showPasswordDialog = false
                            newPassword = ""
                            repeatPassword = ""
                        },
                        //Esta accion ocurre cuando el usuario presiona "confirmar: se limpian los campos y se cierra el diálogo
                        onConfirm = { currentPassword, confirmedNewPassword ->
                            profileViewModel.changeUserPassword(
                                currentPassword,
                                confirmedNewPassword
                            )
                            newPassword = ""
                            repeatPassword = ""
                            showPasswordDialog = false
                        }
                    )
                }

                // Dialog cerrar sesión
                if (showLogoutDialog) {
                    LogoutConfirmationDialog(
                        //En el caso que se seleccione que si se desea cerrar sesión, navega hacia la pantalla de login, evitando que se pueda volver hacia esta pantalla de perfil
                        onConfirm = {
                            // Se limpia la sesión del usuario actualmente logueado
                            profileViewModel.logout()

                            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                            showLogoutDialog = false
                            navController.navigate(AppScreens.LoginScreen.route) {  //Redirige a la pantalla de login
                                // Quita todas las pantallas hasta la especificada (la de perfil)
                                popUpTo(AppScreens.ProfileScreen.route) {
                                    inclusive = true
                                }  //inclusive = true --> Remueve la pantalla actual ProfileScreen
                                launchSingleTop =
                                    true //Evita que se creen múltiples instancias si ya está en el top del stack
                            }
                        },
                        // En el caso que se descarte la opción, es decir, se seleccione que no se desea cerrar sesión, quita el dialog de cerrar sesión de la pantalla
                        onDismiss = { showLogoutDialog = false }
                    )
                }

                // Dialog eliminación de cuenta de usuario/vendedor
                if (showDeleteConfirmDialog) {
                    ConfirmDeleteDialog(
                        onDismiss = { showDeleteConfirmDialog = false },
                        onConfirm = {
                            showDeleteConfirmDialog = false
                            profileViewModel.deleteAccount()
                        }
                    )
                }

                // Dialog o ventana para opciones de foto en caso que exista una foto previamente cargada
                if (showDeletePhotoDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeletePhotoDialog = false },
                        title = { Text("Foto de perfil") },
                        text = { Text("¿Qué quieres hacer con tu foto de perfil?") },
                        confirmButton = {
                            Column {
                                // Cambiar foto
                                Button(
                                    onClick = {
                                        showDeletePhotoDialog = false
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Cambiar foto")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Eliminar foto
                                Button(
                                    onClick = {
                                        showDeletePhotoDialog = false
                                        profileViewModel.deleteProfilePicture()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Eliminar foto")
                                }

                                // Cancelar acción de cambio o eliminación de foto
                                TextButton(
                                    onClick = { showDeletePhotoDialog = false },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        },
                        // No se utiliza ya que no queda estéticamente bien en el diseño, es opcional su uso
                        // Se reemplaza por el TextButton de cancelar acción de arriba
                        dismissButton = {}
                    )
                }
            }
        }
    }
}

/**
 * Valida que una contraseña cumpla con los requisitos mínimos de seguridad.
 *
 * Reglas aplicadas:
 * - Al menos 8 caracteres
 * - Al menos una letra mayúscula
 * - Al menos un dígito numérico
 *
 * @param password Contraseña a validar.
 * @return `true` si la contraseña cumple con el formato requerido,
 *         `false` en caso contrario.
 */
fun isValidPassword(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
    return regex.matches(password)
}

/**
 * Valida que un correo electrónico tenga un formato válido.
 *
 * Utiliza el patrón estándar provisto por la plataforma Android
 * para verificar direcciones de correo electrónico.
 *
 * @param email Dirección de correo electrónico a validar.
 * @return `true` si el formato es válido, `false` en caso contrario.
 */
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Valida que un número de teléfono tenga un formato aceptable.
 *
 * El proceso de validación:
 * - Elimina todos los caracteres que no sean numéricos
 * - Verifica que la longitud resultante sea válida para Argentina
 *   (entre 10 y 15 dígitos, incluyendo prefijos)
 *
 * @param phone Número de teléfono a validar.
 * @return `true` si el número cumple con el formato esperado,
 *         `false` en caso contrario.
 */
fun isValidPhone(phone: String): Boolean {
    // Elimina todo lo que no sea número
    val digitsOnly = phone.filter { it.isDigit() }

    // Argentina: mínimo 10 dígitos, máximo 15 (con prefijos)
    return digitsOnly.length in 10..15
}


/**
 * Composable reutilizable para la edición de un campo del perfil de usuario.
 *
 * Muestra un campo de texto con validación visual inmediata,
 * cambiando los colores del borde y la etiqueta según el estado
 * de validez del contenido.
 *
 * @param label Etiqueta descriptiva del campo.
 * @param value Valor actual del campo.
 * @param isValid Indica si el valor ingresado es válido.
 * @param onValueChange Callback ejecutado cuando el valor cambia.
 */
@Composable
fun EditableProfileItem(
    label: String,
    value: String,
    isValid: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedLabelColor = if (isValid) Color.Green else Color.Red,
                focusedIndicatorColor = if (isValid) Color.Green else Color.Red,
                unfocusedIndicatorColor = if (isValid) Color.Green.copy(0.6f) else Color.Red.copy(0.6f)
            )
        )
    }
}

/**
 * Diálogo para el cambio de contraseña del usuario.
 *
 * Incluye:
 * - Validación de fortaleza de la nueva contraseña
 * - Verificación de coincidencia entre ambas contraseñas
 * - Control de visibilidad del texto de contraseña
 * - Habilitación condicional del botón de confirmación
 *
 * @param newPassword Nueva contraseña ingresada.
 * @param repeatPassword Repetición de la nueva contraseña.
 * @param onPasswordChange Callback al modificar la nueva contraseña.
 * @param onRepeatChange Callback al modificar la contraseña repetida.
 * @param onConfirm Acción a ejecutar al confirmar el cambio.
 * @param onDismiss Acción a ejecutar al cerrar el diálogo.
 */
@Composable
fun ChangePasswordDialog(
    newPassword: String,
    repeatPassword: String,
    onPasswordChange: (String) -> Unit,
    onRepeatChange: (String) -> Unit,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    // Para cambiar la visibilidad de la contraseña actual
    var currentPasswordVisible by remember { mutableStateOf(false) }

    // Para verificar si la contraseña nueva es válida
    val isNewPasswordValid = isValidPassword(newPassword)
    // Para verificar si la contraseña repetida es identica al campo de la nueva contraseña
    val doPasswordsMatch = newPassword == repeatPassword && repeatPassword.isNotBlank()

    // Para cambiar la visibilidad de la contraseña
    var newPasswordVisible by remember { mutableStateOf(false) }
    // Para cambiar la visibilidad de la contraseña repetida
    var repeatPasswordVisible by remember { mutableStateOf(false) }


    // El componente Dialog muestra mensajes emergentes o solicita entradas del usuario en una capa sobre el contenido principal de la app
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                // Campo editable de la clave actual del usuario para agregar capa de seguridad
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña Actual") },
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), //Establece que la contraseña no se pueda ver a simple vista
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val icon = if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Ver contraseña")
                        }
                    },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo editable de la nueva clave para modificar la antigua
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onPasswordChange,
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), //Establece que la contraseña no se pueda ver a simple vista
                    // Define el ojo que muestra u oculta la contraseña
                    trailingIcon = {
                        val icon = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Ver contraseña")
                        }
                    },
                    // Establecemos el color verde y rojo para el campo de nueva contraseña
                    colors = TextFieldDefaults.colors(
                        focusedLabelColor = if (isNewPasswordValid) Color.Green else Color.Red,
                        focusedIndicatorColor = if (isNewPasswordValid) Color.Green else Color.Red,
                        unfocusedIndicatorColor = if (isNewPasswordValid) Color.Green.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo editable de la repetición de la nueva clave para verificar que sea la misma que la nueva clave para modificar la antigua
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = onRepeatChange,
                    label = { Text("Repetir contraseña") },
                    singleLine = true,
                    visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), //Establece que la contraseña no se pueda ver a simple vista
                    // Define el ojo que muestra u oculta la contraseña
                    trailingIcon = {
                        val icon = if (repeatPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Ver contraseña")
                        }
                    },
                    // Establecemos el color verde y rojo para el campo de contraseña repetida
                    colors = TextFieldDefaults.colors(
                        focusedLabelColor = if (doPasswordsMatch) Color.Green else Color.Red,
                        focusedIndicatorColor = if (doPasswordsMatch) Color.Green else Color.Red,
                        unfocusedIndicatorColor = if (doPasswordsMatch) Color.Green.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(currentPassword, newPassword)
                },
                enabled = currentPassword.isNotBlank() && isNewPasswordValid && doPasswordsMatch
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo de confirmación para el cierre de sesión del usuario.
 *
 * Solicita confirmación explícita antes de finalizar la sesión
 * activa, evitando cierres accidentales.
 *
 * @param onConfirm Acción a ejecutar al confirmar el cierre de sesión.
 * @param onDismiss Acción a ejecutar al cancelar la operación.
 */
@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión") },
        text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sí")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

/**
 * Diálogo de confirmación para la eliminación permanente de la cuenta del usuario.
 *
 * Este composable muestra un cuadro de diálogo modal que solicita una confirmación
 * explícita antes de proceder con la eliminación definitiva de la cuenta.
 *
 * Características principales:
 * - Advierte al usuario que la acción es irreversible.
 * - Informa que se eliminarán todos los datos asociados a la cuenta, incluyendo
 *   información personal y, en caso de corresponder, productos del vendedor.
 * - Proporciona acciones claras para confirmar o cancelar la operación.
 *
 * El diálogo se muestra sobre el contenido actual y bloquea la interacción
 * con la pantalla subyacente hasta que el usuario tome una decisión.
 *
 * @param onDismiss Acción que se ejecuta cuando el usuario cancela la operación
 *                  o cierra el diálogo sin confirmar.
 * @param onConfirm Acción que se ejecuta cuando el usuario confirma la eliminación
 *                  de la cuenta.
 */
@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar Cuenta?") },
        text = {
            Text(
                "Esta acción es irreversible. Se borrarán todos tus datos de la aplicación, incluidos tus productos si eres vendedor.\n\n¿Estás seguro de que quieres continuar?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Sí, Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
