package com.undef.localhandsbrambillafunes.ui.screens.profile

import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.undef.localhandsbrambillafunes.ui.navigation.AppScreens
import com.undef.localhandsbrambillafunes.ui.viewmodel.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController,
                  settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>()
) {
    //Necesario para crear los Toast
    val context = LocalContext.current

    // Leemos el valor de la ubicacion en tiempo real
    val userCity by settingsViewModel.userLocation.collectAsState()

    //Variables para los campos editables
    var fullName by remember { mutableStateOf("Juan Perez") }
    var email by remember { mutableStateOf("usuario@gmail.com") }
    var phoneNumber by remember { mutableStateOf("+543411234567") }
    var address by remember { mutableStateOf("Av. Pellegrini 1234") }
    var city by remember { mutableStateOf("Rosario, Santa Fe") }

    //Para validar los datos del usuario
    val isNameValid = fullName.length >= 10
    val isEmailValid = isValidEmail(email)
    val isPhoneValid = isValidPhone(phoneNumber)
    val isAddressValid = address.length >= 10
    val isCityValid = city.length >= 10

    //Para validar que el formulario esté completo para guardar los cambios
    val isFormValid = isNameValid && isEmailValid && isPhoneValid && isAddressValid && isCityValid


    //Variables para dialog de contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    //Variable para dialog de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Estado para guardar la imagen seleccionada
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher que abre la galería
    val launcher = rememberLauncherForActivityResult( //Abre el explorador de archivos para seleccionar una imagen
        contract = ActivityResultContracts.GetContent() //Recibe la ruta que se define en launch mas abajo con el launcher
    ) { uri: Uri? ->
        imageUri = uri // Guarda la URI seleccionada
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
                // Imagen de perfil clickeable
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape) //Recorta el contenido de la box formando un circulo
                        .clickable {
                            //Solo mostrar archivos que comiencen con image/, como image/jpeg, image/png, etc. (de tipo MIME)
                            launcher.launch("image/*") // Abre selector de imágenes, permitiendo cambiar la imagen al hacerle click encima de la foto
                        }) {
                    if (imageUri != null) {
                        //Mostrar imagen seleccionada
                        Image(
                            painter = rememberAsyncImagePainter(imageUri), //Renderiza la imagen directamente desde su URI
                            contentDescription = "Avatar seleccionado",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        //Mostrar imagen por defecto
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Avatar por defecto",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                //Info personal
                //Campos editables: nombre completo, teléfono, domicilio y ciudad
                EditableProfileItem("Nombre completo", fullName, isNameValid) { fullName = it }
                EditableProfileItem("Correo electrónico", email, isEmailValid) { email = it }
                EditableProfileItem("Teléfono", phoneNumber, isPhoneValid) { phoneNumber = it }
                EditableProfileItem("Domicilio", address, isAddressValid) { address = it }
                EditableProfileItem("Ciudad", userCity, isCityValid) { city = it }

                Spacer(Modifier.height(24.dp))

                //Texto clickeable para cambiar la contraseña
                Text("Cambiar contraseña", color = Color.Blue, modifier = Modifier.clickable {
                    showPasswordDialog = true
                })
                //Texto clickeable para ver mis productos
                Text("Mis productos", color = Color.Blue, modifier = Modifier.clickable {
                    Toast.makeText(context, "Mis productos (futuro)", Toast.LENGTH_SHORT).show()
                })
                //Texto clickeable para eliminar la cuenta
                Text("Eliminar cuenta", color = Color.Red, modifier = Modifier.clickable {
                    Toast.makeText(context, "Eliminar cuenta (futuro)", Toast.LENGTH_SHORT).show()
                })

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Cambios guardados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
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

                // 🔐 Dialog cambiar contraseña
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
                        onConfirm = {
                            Toast.makeText(context, "Contraseña cambiada", Toast.LENGTH_SHORT)
                                .show()
                            newPassword = ""
                            repeatPassword = ""
                            showPasswordDialog = false
                        }
                    )
                }

                // 🚪 Dialog cerrar sesión
                if (showLogoutDialog) {
                    LogoutConfirmationDialog(
                        //En el caso que se seleccione que si se desea cerrar sesión, navega hacia la pantalla de login, evitando que se pueda volver hacia esta pantalla de perfil
                        onConfirm = {
                            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                            showLogoutDialog = false
                            navController.navigate(AppScreens.LoginScreen.route) {  //Redirige a la pantalla de login
                                //Quita todas las pantallas hasta la especificada (la de perfil)
                                popUpTo(AppScreens.ProfileScreen.route) {
                                    inclusive = true
                                }  //inclusive = true --> Remueve la pantalla actual ProfileScreen
                                launchSingleTop =
                                    true //Evita que se creen múltiples instancias si ya está en el top del stack
                            }
                        },
                        //En el caso que se descarte la opción, es decir, se seleccione que no se desea cerrar sesión, quita el dialog de cerrar sesión de la pantalla
                        onDismiss = { showLogoutDialog = false }
                    )
                }
            }
        }
    }
}

//Valida que la contraseña tenga al menos 8 caracteres, 1 mayúscula y 1 número
fun isValidPassword(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
    return regex.matches(password)
}

//Valida que el correo electrónico tenga el formato de un email
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

//Valida que el teléfono tenga el formato correcto
fun isValidPhone(phone: String): Boolean {
    val regex = Regex("^\\+\\d{12,15}$") // Ej: +54123456789
    return regex.matches(phone)
}

//Cada campo de texto a editar del perfil
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

//Funcion que se encarga de la validacion de la contraseña y la visibilidad de la misma
@Composable
fun ChangePasswordDialog(
    newPassword: String,
    repeatPassword: String,
    onPasswordChange: (String) -> Unit,
    onRepeatChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    //Para verificar si la contraseña nueva es válida
    val isNewPasswordValid = isValidPassword(newPassword)
    //Para verificar si la contraseña repetida es identica al campo de la nueva contraseña
    val doPasswordsMatch = newPassword == repeatPassword && repeatPassword.isNotBlank()

    //Para cambiar la visibilidad de la contraseña
    var newPasswordVisible by remember { mutableStateOf(false) }
    //Para cambiar la visibilidad de la contraseña repetida
    var repeatPasswordVisible by remember { mutableStateOf(false) }


    //El componente Dialog muestra mensajes emergentes o solicita entradas del usuario en una capa sobre el contenido principal de la app
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onPasswordChange,
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), //Establece que la contraseña no se pueda ver a simple vista
                    //Define el ojo que muestra u oculta la contraseña
                    trailingIcon = {
                        val icon = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Ver contraseña")
                        }
                    },
                    //Establecemos el color verde y rojo para el campo de nueva contraseña
                    colors = TextFieldDefaults.colors(
                        focusedLabelColor = if (isNewPasswordValid) Color.Green else Color.Red,
                        focusedIndicatorColor = if (isNewPasswordValid) Color.Green else Color.Red,
                        unfocusedIndicatorColor = if (isNewPasswordValid) Color.Green.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = onRepeatChange,
                    label = { Text("Repetir contraseña") },
                    singleLine = true,
                    visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), //Establece que la contraseña no se pueda ver a simple vista
                    //Define el ojo que muestra u oculta la contraseña
                    trailingIcon = {
                        val icon = if (repeatPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { repeatPasswordVisible = !repeatPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = "Ver contraseña")
                        }
                    },
                    //Establecemos el color verde y rojo para el campo de contraseña repetida
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
                onClick = onConfirm,
                enabled = isNewPasswordValid && doPasswordsMatch
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

//Abre la ventana de confirmacion para cerrar la sesion
@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
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
